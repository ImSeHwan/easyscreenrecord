package world.junseo.co.kr.easyscreenrecord.Interface;

public abstract interface OnCaptureStateChangedListener {
	  public abstract void onCaptureErrorOccured();

	  public abstract void onCaptureStarted();

	  public abstract void onCaptureStopped(int status);
}
