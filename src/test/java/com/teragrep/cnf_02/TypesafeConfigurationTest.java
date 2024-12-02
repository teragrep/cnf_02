/*
 * Teragrep Configuration Wrapper for Typesafe Config (cnf_02)
 * Copyright (C) 2024 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.cnf_02;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesafeConfigurationTest {

    @Test
    public void testBasicValues() {
        Config typesafe = ConfigFactory.parseFile(new File("src/test/resources/configuration.properties")); // typesafe config is immutable
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        Map<String, String> map = cnf.asMap();

        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("1", map.get("foo"));
        Assertions.assertEquals("foo", map.get("bar"));
        Assertions.assertEquals("true", map.get("fizz"));
    }

    @Test
    public void testEmptyConfiguration() {
        Config typesafe = ConfigFactory.empty();
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        Map<String, String> map = cnf.asMap();

        Assertions.assertTrue(map.isEmpty());
        Assertions.assertTrue(typesafe.isEmpty());
    }

    @Test
    public void testNull() {
        Config typesafe = ConfigFactory.empty();
        typesafe = typesafe.withValue("nullValue", ConfigValueFactory.fromAnyRef(null));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);

        Map<String, String> map = cnf.asMap();

        // null values are not returned from the typesafe config, list stays empty
        Assertions.assertEquals(0, map.size());
    }

    @Test
    public void testList() {
        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");

        Config typesafe = ConfigFactory.empty();
        typesafe = typesafe.withValue("listValue", ConfigValueFactory.fromIterable(list));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);

        Map<String, String> map = cnf.asMap();

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals("[first, second]", map.get("listValue"));
    }

    @Test
    public void testMap() {
        Map<String, String> input = new HashMap<>();
        input.put("foo", "bar");
        input.put("bar", "foo");

        Config typesafe = ConfigFactory.empty();
        typesafe = typesafe.withValue("map.value.path", ConfigValueFactory.fromMap(input));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);

        Map<String, String> map = cnf.asMap();

        // Map gets split to individual paths
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("foo", map.get("map.value.path.bar"));
        Assertions.assertEquals("bar", map.get("map.value.path.foo"));
    }

    @Test
    public void testMapsInList() {
        Map<String, Object> input1 = new HashMap<>();
        input1.put("foo", 1);
        input1.put("bar", 2);
        Map<String, Object> input2 = new HashMap<>();
        input2.put("foo", 1);
        input2.put("bar", 2);
        List<Object> list = new ArrayList<>();
        list.add(input1);
        list.add(input2);

        Config typesafe = ConfigFactory.empty();
        typesafe = typesafe.withValue("list.value.path", ConfigValueFactory.fromIterable(list));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);

        Map<String, String> map = cnf.asMap();

        Assertions.assertEquals(1, map.size());
        // the keys are put in alphabetical order in the maps
        Assertions.assertEquals("[{bar=2, foo=1}, {bar=2, foo=1}]", map.get("list.value.path"));
    }

    @Test
    public void testImmutability() {
        Config typesafe = ConfigFactory.empty();
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        Map<String, String> map = cnf.asMap();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> map.put("biz", "buz")); // immutable
    }

    @Test
    public void testEquals() {
        Config typesafe = ConfigFactory.parseFile(new File("src/test/resources/configuration.properties"));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        TypesafeConfiguration cnf2 = new TypesafeConfiguration(typesafe);

        cnf.asMap();

        Assertions.assertEquals(cnf, cnf2);
    }

    @Test
    public void testNotEquals() {
        Config typesafe = ConfigFactory.parseFile(new File("src/test/resources/configuration.properties"));
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        TypesafeConfiguration cnf2 = new TypesafeConfiguration(typesafe.withoutPath("bar"));

        Assertions.assertNotEquals(cnf, cnf2);
    }

    @Test
    public void testHashCode() {
        Config typesafe = ConfigFactory.parseFile(new File("src/test/resources/configuration.properties"));
        Config typesafe2 = typesafe.withoutPath("bar");
        TypesafeConfiguration cnf = new TypesafeConfiguration(typesafe);
        TypesafeConfiguration cnf2 = new TypesafeConfiguration(typesafe);
        TypesafeConfiguration cnf3 = new TypesafeConfiguration(typesafe2);

        Assertions.assertEquals(cnf.hashCode(), cnf2.hashCode());
        Assertions.assertNotEquals(cnf.hashCode(), cnf3.hashCode());
    }

    @Test
    public void testEqualsVerifier() {
        EqualsVerifier.forClass(TypesafeConfiguration.class).withNonnullFields("config").verify();
    }
}
