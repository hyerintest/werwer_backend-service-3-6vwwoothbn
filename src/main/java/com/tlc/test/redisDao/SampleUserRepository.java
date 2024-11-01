package com.tlc.test.redisDao;

import com.tlc.test.model.user.User;
import org.springframework.data.repository.CrudRepository;

public interface SampleUserRepository extends CrudRepository<User, String> {
}
