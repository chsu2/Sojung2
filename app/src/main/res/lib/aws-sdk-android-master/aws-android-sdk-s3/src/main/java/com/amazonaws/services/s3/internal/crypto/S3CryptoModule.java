/*
 * Copyright 2013-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.services.s3.internal.crypto;

import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

import java.io.File;

/**
 * An internal SPI used to implement different cryptographic modules for use
 * with the S3 encryption client.
 */
public abstract class S3CryptoModule<T extends MultipartUploadContext> {
    public abstract PutObjectResult putObjectSecurely(PutObjectRequest req);

    public abstract S3Object getObjectSecurely(GetObjectRequest req);

    public abstract ObjectMetadata getObjectSecurely(GetObjectRequest req,
            File dest);

    public abstract CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest req);

    public abstract InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest req);

    public abstract UploadPartResult uploadPartSecurely(UploadPartRequest req);

    public abstract CopyPartResult copyPartSecurely(CopyPartRequest req);

    public abstract void abortMultipartUploadSecurely(AbortMultipartUploadRequest req);
}
