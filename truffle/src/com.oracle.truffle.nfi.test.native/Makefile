#
# Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
.PHONY: default

default: ${TARGET}

OBJECTS=bin/array.o bin/global.o bin/null.o bin/numeric.o bin/object.o bin/package.o bin/string.o bin/varargs.o bin/vector.o

CFLAGS+=-g -fPIC -Wall -Werror
LDFLAGS+=-g -lm

ifeq (${OS},darwin)
LDFLAGS+=-undefined dynamic_lookup
endif

ifeq (${OS},solaris)
CFLAGS+=-m64
LDFLAGS+=-m64
endif


${TARGET}: ${OBJECTS}
	@echo LINK $@
	@g++ -shared ${LDFLAGS} $+ -o $@

bin/%.d: src/%.c
	@mkdir -p bin/
	@gcc -MM ${CPPFLAGS} $< | sed 's|\($*\)\.o[ :]*|bin/\1.o $@ : |g' > $@

bin/%.o: src/%.c
	@echo CC $@
	@gcc -c ${CFLAGS} ${CPPFLAGS} -o $@ $<

bin/%.d: src/%.cc
	@mkdir -p bin/
	@g++ -MM ${CPPFLAGS} $< | sed 's|\($*\)\.o[ :]*|bin/\1.o $@ : |g' > $@

bin/%.o: src/%.cc
	@echo CXX $@
	@g++ -c ${CFLAGS} ${CPPFLAGS} -o $@ $<

-include ${OBJECTS:%.o=%.d}
