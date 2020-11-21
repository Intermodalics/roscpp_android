#ifndef __ROS_ANDROID_MAIN_THREAD_H__
#define __ROS_ANDROID_MAIN_THREAD_H__

#include <memory>
#include <mutex>
#include <string>

namespace ros_android {
class MainThread {
  public:
    MainThread();
    explicit MainThread(std::string);
    virtual ~MainThread() = 0;

    typedef std::shared_ptr<MainThread> Ptr;
    static Ptr Instance(void);

    virtual void run() = 0;
    virtual void stop() = 0;
    bool check_ros_master(const std::vector<std::pair<std::string, std::string>> remapings);
  private:
    std::string node_name;

    static std::mutex s_instance_mutex;
    static Ptr s_instance;
};
} // namespace ros_android

#define MY_ROS_ANDROID_MAIN_THREAD(class_name) \
  ros_android::MainThread::Ptr ros_android::MainThread::Instance() { \
    Ptr instance = s_instance; \
    if (!instance) { \
      std::lock_guard<std::mutex> lock(s_instance_mutex); \
      if (!s_instance) { \
        instance = s_instance = std::make_shared<class_name>(); \
      } \
    } \
    return instance; \
  }
#endif // #ifndef __ROS_ANDROID_MAIN_THREAD_H__
