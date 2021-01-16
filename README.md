Quick start examples for [Offscrren Effect Player from Banuba SDK Android]

# Getting Started

1. Get the latest Banuba SDK archive for Android and the client token. Please fill in our form on [form on banuba.com](https://www.banuba.com/face-filters-sdk) website, or contact us via [info@banuba.com](mailto:info@banuba.com).
2. Copy `aar` files from the Banuba SDK archive into `libs` dir:
    `banuba_sdk-release.aar` => `OffscreenEffectPlayer-android-java/libs/`
    `banuba_effect_player-release.aar` => `OffscreenEffectPlayer-android-java/libs/`
3. Copy and Paste your client token into appropriate section of `OffscreenEffectPlayer-android-java/app/src/main/java/com/banuba/offscreen/app/DemoApplication.java 
4. Open the project in Android Studio and run the necessary target using the usual steps.

# Web RTC Integration (VideoFrame as Input)

  // One time 	
  
  offscreenEffectPlayer.setImageProcessListener(result -> {  
		// Do something with result  
        }, handler);  


  // For every Frame  
  
  VideoFrame.I420Buffer i420Buffer = videoframe.getBuffer().toI420();  
  int width = i420Buffer.getWidth();  
  int height = i420Buffer.getHeight();  

  final CameraOrientation cameraOrientation = CameraOrientation.values()[videoframe.getRotation() / 90];  
  final FullImageData.Orientation orientation = new FullImageData.Orientation(cameraOrientation, false, 0);  
  final FullImageData fullImageData = new FullImageData(new Size(width, height),  
  i420Buffer.getDataY(), i420Buffer.getDataU(), i420Buffer.getDataV(), i420Buffer.getStrideY(), i420Buffer.getStrideU(), i420Buffer.getStrideV(), 1, 1, 1, orientation);  
  
  offscreenEffectPlayer.processFullImageData(fullImageData, videoframe::release, videoframe.getTimestampNs());  
