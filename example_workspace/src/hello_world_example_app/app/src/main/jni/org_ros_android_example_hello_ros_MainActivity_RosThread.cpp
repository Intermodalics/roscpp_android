#include <vector>
#include <string>
#include <utility>

#include <ros_android/main_thread.h>
#include "org_ros_android_example_hello_ros_MainActivity_RosThread.h"

using namespace ros_android;

JNIEXPORT void JNICALL Java_org_ros_android_example_hello_1ros_MainActivity_00024RosThread_run
  (JNIEnv *, jobject) {
  MainThread::Instance()->run();
}

JNIEXPORT void JNICALL Java_org_ros_android_example_hello_1ros_MainActivity_00024RosThread_stop
  (JNIEnv *, jobject) {
  MainThread::Instance()->stop();
}

JNIEXPORT jboolean JNICALL Java_org_ros_android_example_hello_1ros_MainActivity_00024RosThread_checkRosMaster
  (JNIEnv *env, jobject, jobject remappings) {
  // C++ vector to populate.
  std::vector<std::pair<std::string, std::string>> remapping_map;

  // List required objects and IDs.
  jclass cls_list = env->GetObjectClass(remappings);
  jmethodID list_size_id = env->GetMethodID(cls_list, "size", "()I");
  jmethodID list_get_id = env->GetMethodID(cls_list, "get", "(I)Ljava/lang/Object;");
  jint list_size = env->CallIntMethod(remappings, list_size_id);

  // Pair required objects and IDs.
  jclass cls_pair = env->FindClass("android/util/Pair");
  jfieldID pair_first_id = env->GetFieldID(cls_pair, "first", "Ljava/lang/Object;");
  jfieldID pair_second_id = env->GetFieldID(cls_pair, "second", "Ljava/lang/Object;");

  for (jint i = 0; i < list_size; i++) {
    jobject jpair = env->CallObjectMethod(remappings, list_get_id, i);
    jstring jfirst = reinterpret_cast<jstring>(env->GetObjectField(jpair, pair_first_id));
    jstring jsecond = reinterpret_cast<jstring>(env->GetObjectField(jpair, pair_second_id));

    std::pair<std::string, std::string> c_pair(std::string(env->GetStringUTFChars(jfirst, nullptr)),
                                               std::string(env->GetStringUTFChars(jsecond, nullptr)));
    remapping_map.push_back(c_pair);
  }


  return MainThread::Instance()->check_ros_master(remapping_map);
}

JNIEXPORT void JNICALL Java_org_ros_android_example_hello_1ros_MainActivity_00024RosThread__1_1RosThread
  (JNIEnv *, jobject) {
  MainThread::Instance();
}