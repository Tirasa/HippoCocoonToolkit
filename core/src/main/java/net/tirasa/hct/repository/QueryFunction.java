/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.tirasa.hct.repository;

import java.util.Calendar;
import javax.jcr.RepositoryException;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum QueryFunction {

    now,
    nowMinusYears,
    nowPlusYears,
    nowMinusMonths,
    nowPlusMonths,
    nowMinusDays,
    nowPlusDays,
    nowMinusHours,
    nowPlusHours,
    nowPlusMinutes,
    nowMinusMinutes,
    nowMinusSeconds,
    nowPlusSeconds;

    private static final Logger LOG = LoggerFactory.getLogger(QueryFunction.class);

    public static String call(final String functionCall) throws RepositoryException {
        QueryFunction function = now;

        for (QueryFunction qf : values()) {
            if (functionCall.startsWith(qf.name() + "(")) {
                function = qf;
            }
        }

        final Calendar calendar = Calendar.getInstance();
        int param = 0;
        if (function != now) {
            int startPos = function.name().length() + 1;
            int endPos = functionCall.lastIndexOf(')');
            try {
                param = Integer.valueOf(functionCall.substring(startPos, endPos));
            } catch (NumberFormatException e) {
                LOG.error("Could not parse, reverting to 0", e);
                param = 0;
            }
        }

        switch (function) {
            case nowMinusYears:
                calendar.add(Calendar.YEAR, -param);
                break;

            case nowPlusYears:
                calendar.add(Calendar.YEAR, param);
                break;

            case nowMinusMonths:
                calendar.add(Calendar.MONTH, -param);
                break;

            case nowPlusMonths:
                calendar.add(Calendar.MONTH, param);
                break;

            case nowMinusDays:
                calendar.add(Calendar.DAY_OF_YEAR, -param);
                break;

            case nowPlusDays:
                calendar.add(Calendar.DAY_OF_YEAR, param);
                break;

            case nowMinusHours:
                calendar.add(Calendar.HOUR, -param);
                break;

            case nowPlusHours:
                calendar.add(Calendar.HOUR, param);
                break;

            case nowMinusMinutes:
                calendar.add(Calendar.MINUTE, -param);
                break;

            case nowPlusMinutes:
                calendar.add(Calendar.MINUTE, param);
                break;

            case nowMinusSeconds:
                calendar.add(Calendar.SECOND, -param);
                break;

            case nowPlusSeconds:
                calendar.add(Calendar.SECOND, param);
                break;

            case now:
            default:
        }

        return ValueFactoryImpl.getInstance().createValue(calendar).getString();
    }
}
