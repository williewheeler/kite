/*
 * Copyright (c) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zkybase.kite;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation indicating methods to be protected by one or more guards. A future version of Kite will support using this
 * annotation on classes to guard all methods.
 * </p>
 * <p>IMPORTANT: The ordering of the guards is significant: we process guards earlier in the list before guards later in
 * the list. This is very important, since you don't want to put a guard that generates client-induced exceptions behind
 * a circuit breaker. For example, you don't want a rate limiter behind a circuit breaker because then the client can
 * take down the service by exceding its rate limit. A future version on the library will address this potential issue
 * in some way, perhaps by having a fixed order for the guards, perhaps by having circuit breakers ignore client
 * exceptions, or some combination of the two.
 * </p>
 *  
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GuardedBy {

	String[] value() default "";
}
