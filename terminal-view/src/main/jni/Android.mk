LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE:= libiTerminal
LOCAL_SRC_FILES:= termux.c
include $(BUILD_SHARED_LIBRARY)