/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry
import org.junit.Test

class GolangConfigurationTest {
    GolangConfiguration configuration = new GolangConfiguration('build')

    @Test
    void 'dependency registry should be isolated'() {
        GolangConfiguration build = new GolangConfiguration('build')
        GolangConfiguration test = new GolangConfiguration('test')
        assert build.dependencyRegistry instanceof DefaultDependencyRegistry
        assert !build.dependencyRegistry.is(test.dependencyRegistry)
    }

    @Test
    void 'getting dependencies should succeed'() {
        assert configuration.getDependencies().isEmpty()
    }

}
