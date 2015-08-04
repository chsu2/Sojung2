/*
 * Copyright 2010-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.services.kinesis.model.transform;

import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.kinesis.model.*;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.*;
import com.amazonaws.transform.*;
import com.amazonaws.util.json.AwsJsonReader;

/**
 * Hash Key Range JSON Unmarshaller
 */
public class HashKeyRangeJsonUnmarshaller implements Unmarshaller<HashKeyRange, JsonUnmarshallerContext> {

    public HashKeyRange unmarshall(JsonUnmarshallerContext context) throws Exception {
        HashKeyRange hashKeyRange = new HashKeyRange();

        AwsJsonReader reader = context.getReader();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            
            if (name.equals("StartingHashKey")) {
                hashKeyRange.setStartingHashKey(StringJsonUnmarshaller.getInstance().unmarshall(context));
            } else 
            if (name.equals("EndingHashKey")) {
                hashKeyRange.setEndingHashKey(StringJsonUnmarshaller.getInstance().unmarshall(context));
            } else 
            {
                reader.skipValue();
            }
            
        }
        reader.endObject();
        
        return hashKeyRange;
    }

    private static HashKeyRangeJsonUnmarshaller instance;
    public static HashKeyRangeJsonUnmarshaller getInstance() {
        if (instance == null) instance = new HashKeyRangeJsonUnmarshaller();
        return instance;
    }
}
    