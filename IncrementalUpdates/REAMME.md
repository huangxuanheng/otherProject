增量安装


原理：两个版本apk，分别为old.apk,new.apk。根据这两个apk的不同部分生成一个新的文件patch,然后根据再使用该patch文件与手机端安装的apk，合成一个新的apk,假设是mix.apk。再拿该mix.apk的md5与
new.apk的md5作比较，如果这两个apk对应的md5一致，则说明增量合成新apk成功，否则合成失败

步骤：
1.分别准备一个老版本和新版本的apk
2.根据新老版本apk生成差异部分patch文件
3.将apk存放到服务器端，当客户端请求时下载到客户端
4.客户端下载了patch文件后，再与客户端安装的对应apk进行合成新的apk，这个新的apk即是最新版本
5.检查两个新版mix.apk和new.apk的md5是否一致
