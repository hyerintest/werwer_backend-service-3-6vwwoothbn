package com.tlc.test.model.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@RedisHash("user")
public class User {
	String id;
	String password;
}
