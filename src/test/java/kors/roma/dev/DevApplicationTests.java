package kors.roma.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import kors.roma.dev.common.Logger;
import kors.roma.dev.common.RabbitUserEventPublisher;
import kors.roma.dev.common.UserPublishAdapter;
import kors.roma.dev.dto.request.RegistrationRequest;
import kors.roma.dev.model.User;
import kors.roma.dev.repository.UserRepository;
import kors.roma.dev.service.AuthenticationLifecycleService;

@SpringBootTest
class DevApplicationTests {
	@Autowired
	private UserPublishAdapter<RabbitUserEventPublisher> adapter;

	@Test
	void contextLoads() {
	}

	@Test
	void checkThatUserPersistenceWorks(@Autowired BCryptPasswordEncoder encoder,
		@Autowired UserRepository userRepo,@Autowired Logger logger,
		@Autowired AuthenticationLifecycleService userService)
	{
		User user = getNewUser(encoder,userService,userRepo);
		userRepo.save(user);
		userRepo.deleteById(user.getUid());
		logger.logInfo("Deleted user");
	}

	@Test
	void checkThatPublisherWorks() throws InterruptedException{
		// in fact it's kinda hard to test for deadlocks and stuff like that
		// proving the correctness seems like a better way.
		List<Runnable> tasks = new ArrayList<>(1000);
		for(int i = 0; i < 10; i++){
			int j = i;
			Runnable task = () -> {
				try {
					var user = new User(UUID.randomUUID(),
							"Frodo" + j,
							"Frodo",
							"Baggins",
							"secretPassword",
							"frodo" + j + "@shire.com");
					adapter.publishUserCreated(user);
					System.out.println("publishing user");
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			tasks.add(task);
		}
		var virtualThreadsPool = Executors.newVirtualThreadPerTaskExecutor();
		for(var t : tasks){
			virtualThreadsPool.execute(t);
		}
		virtualThreadsPool.shutdown();
		virtualThreadsPool.awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test 
	void checkThatNPlusOneIsSolved(){
		//enable actual database query logs to see
		//the number of queries executed when iterating on root entities
		//and getting their associated entities (N+1 is dogshit for performance)
	}

	//return null on exception
	private User getNewUser(BCryptPasswordEncoder encoder, AuthenticationLifecycleService userService,
		UserRepository userRepo){
		try {
			boolean registered = userService.registerUser(
				new RegistrationRequest("Admin", "Roman123456789",
				"Roman","Kors","korsroma@gmail.com"));
			if(registered){
				System.out.println("REGISTERED");
			}
			var usr = userRepo.findByUsername(("Admin"));
			return usr.get();
		} 
		catch (Exception e) {
			return null;
		}
	}
}
