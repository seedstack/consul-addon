/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.consul.internal;

import org.seedstack.shed.exception.ErrorCode;

enum ConsulErrorCode implements ErrorCode {
    CANNOT_CREATE_CLIENT,
    CLASS_NOT_ACCESSIBLE,
    CLASS_NOT_INSTANTIATE,
    NUMBER_FORMAT_EXCEPTION
}
