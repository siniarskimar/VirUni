package io.github.siniarski.viruni;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.boot.autoconfigure.SpringBootApplication
public class SpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApplication.class, args);
	}

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner startup() {
		return args -> {
			if(accountRepository.countByRole(AccountRole.ADMIN) == 0) {
				accountRepository.save(
						new Account(
								"admin",
								passwordEncoder.encode("administrator"),
								"Admin",
								"Admin",
								AccountRole.ADMIN));
			}
		};
	}
}
