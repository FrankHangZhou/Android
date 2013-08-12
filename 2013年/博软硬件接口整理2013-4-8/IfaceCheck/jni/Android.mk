# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH:= $(call my-dir)

# 指纹的SO封装成动态库
include $(CLEAR_VARS)     
LOCAL_MODULE    := libzhiwen   
LOCAL_SRC_FILES := libfingerprintlyt.so     
include $(PREBUILT_SHARED_LIBRARY) 
##################################################
# RFID的我们也封装成动态库
include $(CLEAR_VARS)     
LOCAL_MODULE    := librfidoo
LOCAL_SRC_FILES := librfid.so
LOCAL_LDLIBS    := -llog
include $(PREBUILT_SHARED_LIBRARY) 
###################################################




include $(CLEAR_VARS)
LOCAL_MODULE    := libcheck
LOCAL_SRC_FILES := second.c
LOCAL_LDLIBS    := -llog 
LOCAL_SHARED_LIBRARIES := libzhiwen  librfidoo

include $(BUILD_SHARED_LIBRARY)
