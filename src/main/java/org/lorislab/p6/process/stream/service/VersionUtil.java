/*
 * Copyright 2019 lorislab.org.
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

package org.lorislab.p6.process.stream.service;

public class VersionUtil {

    private static final String SUFFIX_SNAPSHOT = "-SNAPSHOT";

    private VersionUtil() {
        // empty constructor
    }

    /**
     * Compare two version strings. Return true only if the newVersion is greater then the stored one so a version update is needed
     *
     * @param newVersion    new deployed version
     * @param storedVersion stored version in database
     * @return true only if the newVersion is greater then the stored one
     */
    public static boolean versionUpdateNeeded(String newVersion, String storedVersion) {

        String ver1 = newVersion;
        int var1 = newVersion.indexOf(SUFFIX_SNAPSHOT);
        if (var1 != -1) {
            ver1 = newVersion.substring(0, var1);
        }

        String ver2 = storedVersion;
        boolean snapshot = false;
        int var2 = storedVersion.indexOf(SUFFIX_SNAPSHOT);
        if (var2 != -1) {
            ver2 = storedVersion.substring(0, var2);
            snapshot = true;
        }

        String[] vals1 = ver1.split("\\.");
        String[] vals2 = ver2.split("\\.");

        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = convertVersionNumberToIntAndCompare(vals1[i], vals2[i]);
            return (diff > 0);
        } else {
            // the strings are equal or one string is a substring of the other
            // or snapshot
            // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
            if (snapshot) {
                return true;
            }
            return (vals1.length > vals2.length);
        }
    }

    /**
     * Return true if the newVersion is greater then stored version.
     *
     * @param newVersion    new version
     * @param storedVersion stored version from DB
     * @return true if the newVersion is greater then stored version.
     */
    private static int convertVersionNumberToIntAndCompare(String newVersion, String storedVersion) {
        try {
            Integer uploaded = Integer.valueOf(newVersion);
            Integer stored = Integer.valueOf(storedVersion);
            return uploaded.compareTo(stored);
        } catch (Exception e) {
            return newVersion.compareTo(storedVersion);
        }
    }

}
