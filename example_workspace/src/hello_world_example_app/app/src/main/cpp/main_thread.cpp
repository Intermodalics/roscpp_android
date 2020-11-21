#include <stdarg.h>
#include <sstream>
#include <utility>
#include <vector>

#include <ros/ros.h>
#include <ros_android/main_thread.h>


namespace ros_android {
    std::mutex MainThread::s_instance_mutex;
    MainThread::Ptr MainThread::s_instance;
    MainThread::MainThread() : node_name("ros_android_node") {}
    MainThread::MainThread(std::string name) : node_name(name) {}
    MainThread::~MainThread() {}

    bool MainThread::check_ros_master(const std::vector<std::pair<std::string, std::string>> remappings) {
        bool rv;

        ros::init(remappings, node_name.c_str());
        rv = ros::master::check();
        if (rv) {
            ROS_INFO("ROS MASTER IS UP!");
        } else {
            ROS_INFO("NO ROS MASTER.");
        }
        ros::start();
        return rv;
    }
}