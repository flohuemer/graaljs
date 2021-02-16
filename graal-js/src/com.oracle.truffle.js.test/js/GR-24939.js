/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

load('assert.js');

var argumentsEvaluated = false;
var f = function () {
    argumentsEvaluated = true;
};
assertThrows(function () {
    (undefined?.foo)(f());
}, TypeError);
assertSame(true, argumentsEvaluated);
