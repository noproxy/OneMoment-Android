/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.transitionseverywhere;

import android.util.SparseArray;
import android.view.View;

import com.transitionseverywhere.utils.ArrayMap;
import com.transitionseverywhere.utils.LongSparseArray;

class TransitionValuesMaps {
    ArrayMap<View, TransitionValues> viewValues =
            new ArrayMap<View, TransitionValues>();
    SparseArray<View> idValues = new SparseArray<View>();
    LongSparseArray<View> itemIdValues = new LongSparseArray<View>();
    ArrayMap<String, View> nameValues = new ArrayMap<String, View>();
}
