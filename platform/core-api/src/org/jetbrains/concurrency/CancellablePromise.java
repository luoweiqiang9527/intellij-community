// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.concurrency;

import java.util.concurrent.Future;

interface CancellablePromise<T> extends Promise<T>, Future<T> {
  void cancel();
}