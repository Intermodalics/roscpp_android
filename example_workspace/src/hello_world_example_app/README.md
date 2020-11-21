# Hello ROS (pubsub example - native Android)

This is a hello ROS example app. It publishes a heartbeat to `/chatter` topic; when a message is received it is also logged using `ROS_INFO`.
The heartbeat looks like this:

    data: "hello world from android ndk __COUNTER_VALUE__"

## Usage

1. Build the samples with `install` script using `--samples` option.
If you already have compiled the basic catkin workspace, you can enter the docker container (`docker/run.sh`) and build the samples using `build_catkin_workspace.sh` script using `-w` and `-p` options like this (replace paths with your current configuration if necessary):

		/opt/ros_android/scripts/build_catkin_workspace.sh -w /opt/ros_android/example_workspace/ -p /opt/ros_android/output/ -v 1 -b Debug

2. The APK file should be inside `app/build/outputs/apk/debug`. Install it in your android device using adb -d install as explained here:
        <https://developer.android.com/studio/build/building-cmdline>

3. Execute roscore in a terminal with ros sourced. Remember to export first your ip address:
        
        export ROS_IP=__YOUR_IP_ADDRESS__

4. Subscribe to /chatter in another terminal with ros setup sourced:
    
        rostopic echo /chatter

  You should see the heartbeat coming out.

5. Optionally, publish to /chatter, in another terminal with ros setup sourced and ROS_IP exported:

        rostopic pub /chatter std_msgs/String "__YOUR_MESSAGE__" -r __PUBLISHING_RATE__

  The message you have sent is logged in Android, you can check the result with logcat:

		adb logcat

  You should see the heartbeat messages as well as any message you publish to `/chatter` topic in Android's log.

### Remapping arguments

You can also remap topics as if you were launching a ROS console application. For example, to remap the topic enter the following:

	/chatter:=/custom_topic

In this case, you will see the output in `/custom_topic` instead.
