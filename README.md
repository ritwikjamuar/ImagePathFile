# ImagePathFile

This project is about obtaining the path of Image File.

There are two aspects for retrieving an Image File:

1. Image will be captured from the Camera Application of the device.
2. Image exists in the Device's Internal or External Storage which can be accessed through Gallery/Photos application.

The motive of this project is to obtain the actual file-path of the Image File in the Android Device.

## Retrieving Image Path from Camera

I have refered the [Android Developer's Guide] (https://developer.android.com/training/camera/photobasics.html) to save the image file in the Internal Storage.

Before creating a file, one has to take permissions, which is, `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`, otherwise one will be denied for creating the file/follder in the specified location.

### Step 1 : Check for Permission

```java
if ( !(
    ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
    &&
    ContextCompat.checkSelfPermission ( getApplicationContext (), Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
    ) )
```
### Step 2 : If the Permission is granted by the User, take the image using `dispatchTakePictureIntent()` else Request Permission at runtime as below:

