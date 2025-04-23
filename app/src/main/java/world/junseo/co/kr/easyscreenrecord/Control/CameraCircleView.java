package world.junseo.co.kr.easyscreenrecord.Control;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;
import world.junseo.co.kr.easyscreenrecord.glescore.BasePolygon;
import world.junseo.co.kr.easyscreenrecord.glescore.CameraUtils;
import world.junseo.co.kr.easyscreenrecord.glescore.GlUtil;


public class CameraCircleView extends GLSurfaceView {
	CameraCircleViewRenderer mRenderer;

	public CameraCircleView (Context context , int filter, int Shape) {
		super ( context );
		setEGLContextClientVersion ( 2 );
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);	
		getHolder().setFormat(PixelFormat.TRANSLUCENT);

		mRenderer = new CameraCircleViewRenderer(this, context, filter, Shape);
		
		setRenderer ( mRenderer );
		setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
	}

	public void surfaceCreated ( SurfaceHolder holder ) {
		super.surfaceCreated ( holder );
	}

	public void surfaceDestroyed ( SurfaceHolder holder ) {
		 mRenderer.close();
		 super.surfaceDestroyed ( holder );
	}

	public void surfaceChanged (SurfaceHolder holder, int format, int w, int h ) {
		super.surfaceChanged ( holder, format, w, h );
	}
}


//Renderer
class CameraCircleViewRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
	private static final String TAG = "GLSurfaceViewRenderer";

    static final int FILTER_NONE = 0;			// 효과없을경우
    static final int FILTER_BLACK_WHITE = 1;	// 흑백
    static final int FILTER_BLUR = 2;			// 블러
    static final int FILTER_SHARPEN = 3;		// 날카로운 효과
    static final int FILTER_EDGE_DETECT = 4;	// 외각선 노출
    static final int FILTER_EMBOSS = 5;   		// 엠보싱처리
	
	private CameraCircleView mView;
	private Context mContext;
	private WindowManager mWindowManager;
	private int mFilter;
	private float[] mKernel = null;
	private float mColorAdj = 0.0f;
	private float[] mTexOffset = null;
	private int mTextureTarget;
	
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int muKernelLoc;
    private int muTexOffsetLoc;
    private int muColorAdjustLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    
	private FloatBuffer mFVertexBuffer;
	private FloatBuffer mFTextureBuffer;
	private ShortBuffer mIndexBuffer;
	private int numOfIndices = 0;    
	
	
	private int[] hTex;
	private FloatBuffer pVertex;
	private FloatBuffer pTexCoord;
	private ProgramType mProgramType;	
    public enum ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW, TEXTURE_EXT_FILT
    }
	
	private Camera mCamera;
	private SurfaceTexture mSTexture;
	private int mCameraPreviewWidth, mCameraPreviewHeight;
	private int mSurfaceWidth, mSurfaceHeight; 
	
    private static final int REQ_CAMERA_WIDTH = 1280;
    private static final int REQ_CAMERA_HEIGHT = 720;
    private static final int REQ_CAMERA_FPS = 30;
    
    private static final int DEFAULT_ZOOM_PERCENT = 0;      // 0-100
    private static final int DEFAULT_SIZE_PERCENT = 100;     // 0-100
    private static final int DEFAULT_ROTATE_PERCENT = 0;    // 0-100    
    
    private float[] mDisplayProjectionMatrix = new float[16];
    private float[] mScratchMatrix = new float[16];
    private float mPosX, mPosY;
    private boolean mIsRotChange = false;
    private int mOldRot = -1;	
    private int mShapeCount = 90;
	
	private boolean mUpdateST = false;
	BasePolygon polygonTemp;

	CameraCircleViewRenderer ( CameraCircleView view ) {
		 this(view, null);
	}
	
	CameraCircleViewRenderer ( CameraCircleView view , Context context) {
		this(view, null, FILTER_NONE, 90);	 
	}

	CameraCircleViewRenderer (CameraCircleView view , Context context, int filter, int shape) {
		 mView = view;
		 mWindowManager = ((WindowManager)view.getContext().getSystemService("window"));
		 mContext = context;
		 mFilter = filter;
		 mShapeCount = shape;
	}	
	
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		     
		 initTex();
		 mSTexture = new SurfaceTexture( hTex[0] );
		 mSTexture.setOnFrameAvailableListener(this);
		 
		 if(mContext != null) {
			 try {
				 openCamera(REQ_CAMERA_WIDTH, REQ_CAMERA_HEIGHT, REQ_CAMERA_FPS);
	
				 // 카메라가 오픈되는 시점에서 좌표값을 설정 한다.
				 if(mShapeCount < 5)
					 polygonTemp = new BasePolygon(0, 0, 0, 0.7f, mShapeCount, mCameraPreviewWidth, mCameraPreviewHeight);
				 else
					 polygonTemp = new BasePolygon(0, 0, 0, 0.5f, mShapeCount, mCameraPreviewWidth, mCameraPreviewHeight);
				 
				 // 필터 설정
				 SetFilter();			 
	
				 // 쉐이더 설정
				 SetShader();
			 
				 mCamera.setPreviewTexture(mSTexture);
			 } catch ( IOException ioe ) {
				 //throw new RuntimeException("Unable to setPreviewTexture camera");
				 //Log.e(TAG, "Unable to setPreviewTexture camera");
				 //Toast.makeText(mContext, "카메라 오픈 불가" ,Toast.LENGTH_LONG).show();
				 //CommFunction.ToastEx(mContext, "카메라 오픈 불가");
				 JLog.e(TAG, "카메라 오픈 불가 : " + ioe.toString());
			 }			 
		 }
		
		 GLES20.glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );
	}	

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
        //Log.d(TAG, "starting camera preview");
        try {
			mSurfaceWidth = width;
			mSurfaceHeight = height;
			GLES20.glViewport( 0, 0, width, height );
			UpdateGeometry(width, height);		 
			mCamera.startPreview();	
         } catch (Exception ex) {
        	 //throw new RuntimeException("Unable to startPreview camera");
        	//Log.e(TAG, "Unable to startPreview camera");
        	//Toast.makeText(mContext, "카메라 설정 불가" ,Toast.LENGTH_LONG).show();
			JLog.e(TAG, "카메라 설정 불가 : " + ex.toString());
         }
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		UpdateGeometry(mSurfaceWidth , mSurfaceHeight );
		
		// TODO Auto-generated method stub
		GLES20.glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );
		GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );
		
		synchronized(this) {
			if ( mUpdateST ) {
				mSTexture.updateTexImage();
				mUpdateST = false;
			}		
		}
		 
		this.mFVertexBuffer = polygonTemp.getVertexBuffer();
		this.mFTextureBuffer = polygonTemp.getTextureBuffer();
		this.mIndexBuffer = polygonTemp.getIndexBuffer();
		this.numOfIndices = polygonTemp.getNumberOfIndeices();
		this.mFVertexBuffer.position(0);
		this.mIndexBuffer.position(0);
		this.mFTextureBuffer.position(0);
		
		GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, hTex[0]);

        // Copy the model / view / projection matrix over.
        //GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mScratchMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, GlUtil.IDENTITY_MATRIX, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, 3, GLES20.GL_FLOAT, false, 12, mFVertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, 8, mFTextureBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Populate the convolution kernel, if present.
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0);
            GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0);
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdj);
        }

        // Draw the rect.
        GLES20.glDrawElements(GL10.GL_TRIANGLES, this.numOfIndices, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
	}

	@Override
	public synchronized void onFrameAvailable(SurfaceTexture arg0) {
		 mUpdateST = true;
		 GLES20.glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );
		 GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );
		 mView.requestRender();		
	}	

	public void close()	{
		mUpdateST = false;
		if(mSTexture != null)
			mSTexture.release();
		if(mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			//mCamera = null;
		}
		deleteTex();
	}

	
	private void initTex() {
		 hTex = new int[1];
		 GLES20.glGenTextures ( 1, hTex, 0 );
		 GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		 GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		 GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		 GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		 GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	}

	
	private void deleteTex() {
		GLES20.glDeleteTextures ( 1, hTex, 0 );
	}
	
	
	private void UpdateGeometry(int width, int height) {
		int rotWindow = mWindowManager.getDefaultDisplay().getRotation();
		
		if(rotWindow != mOldRot) {
			mIsRotChange = true;
		}
		
		if(mIsRotChange) {				
			Matrix.orthoM(mDisplayProjectionMatrix, 0, 0, width, 0, height, -1, 1);
			mPosX = width / 2.0f;
			mPosY = height / 2.0f;
			 
			int smallDim = Math.min(width, height);
			
			float scaled = smallDim * (DEFAULT_SIZE_PERCENT / 100.0f);
			float cameraAspect = (float) mCameraPreviewWidth / mCameraPreviewHeight;
			int newWidth = Math.round(scaled * cameraAspect);
			int newHeight = Math.round(scaled);
			
			float zoomFactor = 1.0f - (DEFAULT_ZOOM_PERCENT / 100.0f);
			
			int rotAngleBase = DEFAULT_ROTATE_PERCENT;
			
			switch(rotWindow) {
				case 0 : rotAngleBase = 25; break;
				case 1 : rotAngleBase = 50; break;
				case 2 : rotAngleBase = 0; break;
				case 3 : rotAngleBase = 0; break;
			}
			int rotAngle = Math.round(360 * (rotAngleBase / 100.0f));
			
			float[] mMatrixTemp = new float[16];
			Matrix.setIdentityM(mMatrixTemp, 0);
			Matrix.translateM(mMatrixTemp, 0, mPosX, mPosY, 0.0f);
			if (rotAngle != 0.0f) {
				Matrix.rotateM(mMatrixTemp, 0, rotAngle, 0.0f, 0.0f, 1.0f);
			}
			
			Matrix.scaleM(mMatrixTemp, 0, newWidth, newHeight, 1.0f);
			
	        Matrix.multiplyMM(mScratchMatrix, 0, mDisplayProjectionMatrix, 0, mMatrixTemp, 0);
	        
	        mOldRot = rotWindow ;
	        mIsRotChange = false;
		}
	}
	
	private void SetShader() {
        switch (mProgramType) {
            case TEXTURE_2D:
                mTextureTarget = GLES20.GL_TEXTURE_2D;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
                break;
            case TEXTURE_EXT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
                break;
            case TEXTURE_EXT_BW:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_BW);
                break;
            case TEXTURE_EXT_FILT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_FILT);
                break;
            default:
                throw new RuntimeException("Unhandled type " + mProgramType);
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        //Log.d(TAG, "Created program " + mProgramHandle + " (" + mProgramType + ")");

        // get locations of attributes and uniforms
        
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel");
        
        if (muKernelLoc < 0) {
            // no kernel in this one
            muKernelLoc = -1;
            muTexOffsetLoc = -1;
            muColorAdjustLoc = -1;
        } else {
            // has kernel, must also have tex offset and color adj
            muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
            GlUtil.checkLocation(muTexOffsetLoc, "uTexOffset");
            muColorAdjustLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColorAdjust");
            GlUtil.checkLocation(muColorAdjustLoc, "uColorAdjust");

            setTexSize(256, 256);
        }
	}
		
	
	private static int loadShader (String vss, String fss ) {
		 int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		 GLES20.glShaderSource(vshader, vss);
		 GLES20.glCompileShader(vshader);
		 int[] compiled = new int[1];
		 GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		 if (compiled[0] == 0) {
			   //Log.e("Shader", "Could not compile vshader");
			   //Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
			   GLES20.glDeleteShader(vshader);
			   vshader = 0;
		 }
	
		 int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		 GLES20.glShaderSource(fshader, fss);
		 GLES20.glCompileShader(fshader);
		 GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		 if (compiled[0] == 0) {
			   //Log.e("Shader", "Could not compile fshader");
			   //Log.v("Shader", "Could not compile fshader:"+GLES20.glGetShaderInfoLog(fshader));
			   GLES20.glDeleteShader(fshader);
			   fshader = 0;
		 }
	
		 int program = GLES20.glCreateProgram();
		 GLES20.glAttachShader(program, vshader);
		 GLES20.glAttachShader(program, fshader);
		 GLES20.glLinkProgram(program);
		     
		 return program;
	}
	
	
	private void openCamera(int desiredWidth, int desiredHeight, int desiredFps) {
         if (mCamera != null) {
        	 mCamera.release();
        	 mCamera = null;
             //throw new RuntimeException("camera already initialized");
         }

         Camera.CameraInfo info = new Camera.CameraInfo();

         int numCameras = Camera.getNumberOfCameras();
         try {
             for (int i = 0; i < numCameras; i++) {
                 Camera.getCameraInfo(i, info);
                 if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                     mCamera = Camera.open(i);
                     break;
                 }
             }
             
             if (mCamera == null) {
                 //Log.d(TAG, "No front-facing camera found; opening default");
                 mCamera = Camera.open();    // opens first back-facing camera
             }
             if (mCamera == null) {
            	 //Log.e(TAG, "There is no usable camera");
                 throw new RuntimeException("There is no usable camera");
             }             
         }
         catch (Exception e){
        	 //Log.e(TAG, "Unable to open camera");
        	 throw new RuntimeException("Unable to open camera");
         }         

         Camera.Parameters parms = mCamera.getParameters();

         CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);

         // Give the camera a hint that we're recording video.  This can have a big
         // impact on frame rate.
         parms.setRecordingHint(false);

         mCamera.setParameters(parms);

         int[] fpsRange = new int[2];
         Camera.Size mCameraPreviewSize = parms.getPreviewSize();
         parms.getPreviewFpsRange(fpsRange);
         
         String previewFacts = mCameraPreviewSize.width + "x" + mCameraPreviewSize.height;
         if (fpsRange[0] == fpsRange[1]) {
             previewFacts += " @" + (fpsRange[0] / 1000.0) + "fps";
         } else {
             previewFacts += " @[" + (fpsRange[0] / 1000.0) +
                     " - " + (fpsRange[1] / 1000.0) + "] fps";
         }
         //Log.i(TAG, "Camera config: " + previewFacts);
         
         mCameraPreviewWidth = mCameraPreviewSize.width;
         mCameraPreviewHeight = mCameraPreviewSize.height;         
     }
	
	 
	public void SetFilter() {
		//참고 자료 -> http://en.wikipedia.org/wiki/Kernel_(image_processing)	
	    switch (mFilter) {
	        case FILTER_NONE:
	        	mProgramType = ProgramType.TEXTURE_EXT;
	            break;
	        case FILTER_BLACK_WHITE:
	            // (In a previous version the TEXTURE_EXT_BW variant was enabled by a flag called
	            // ROSE_COLORED_GLASSES, because the shader set the red channel to the B&W color
	            // and green/blue to zero.)
	        	mProgramType = ProgramType.TEXTURE_EXT_BW;
	            break;
	        case FILTER_BLUR:
	        	mProgramType = ProgramType.TEXTURE_EXT_FILT;
	        	mKernel = new float[] {
	                    1f/16f, 2f/16f, 1f/16f,
	                    2f/16f, 4f/16f, 2f/16f,
	                    1f/16f, 2f/16f, 1f/16f };
	            break;
	        case FILTER_SHARPEN:
	        	mProgramType = ProgramType.TEXTURE_EXT_FILT;
	        	mKernel = new float[] {
	                    0f, -1f, 0f,
	                    -1f, 5f, -1f,
	                    0f, -1f, 0f };
	            break;
	        case FILTER_EDGE_DETECT:
	        	mProgramType = ProgramType.TEXTURE_EXT_FILT;
	        	mKernel = new float[] {
	                    -1f, -1f, -1f,
	                    -1f, 8f, -1f,
	                    -1f, -1f, -1f };
	            break;
	        case FILTER_EMBOSS:
	        	mProgramType = ProgramType.TEXTURE_EXT_FILT;
	        	mKernel = new float[] {
	                    2f, 0f, 0f,
	                    0f, -1f, 0f,
	                    0f, 0f, -1f };
	            mColorAdj = 0.5f;
	            break;
	        default:
	            throw new RuntimeException("Unknown filter mode " + mFilter);
	    }
	}	 
 
    public void setTexSize(int width, int height) {
        float rw = 1.0f / width;	
        float rh = 1.0f / height;	

        mTexOffset = new float[] {
            -rw, -rh,   0f, -rh,    rw, -rh,
            -rw, 0f,    0f, 0f,     rw, 0f,
            -rw, rh,    0f, rh,     rw, rh
        };
    }
    
    
    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // Fragment shader that converts color to black & white with a simple transformation.
    private static final String FRAGMENT_SHADER_EXT_BW =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
            "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
            "}\n";

    // Fragment shader with a convolution filter.  The upper-left half will be drawn normally,
    // the lower-right half will have the filter applied, and a thin red line will be drawn
    // at the border.
    //
    // This is not optimized for performance.  Some things that might make this faster:
    // - Remove the conditionals.  They're used to present a half & half view with a red
    //   stripe across the middle, but that's only useful for a demo.
    // - Unroll the loop.  Ideally the compiler does this for you when it's beneficial.
    // - Bake the filter kernel into the shader, instead of passing it through a uniform
    //   array.  That, combined with loop unrolling, should reduce memory accesses.
    public static final int KERNEL_SIZE = 9;
    private static final String FRAGMENT_SHADER_EXT_FILT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "#define KERNEL_SIZE " + KERNEL_SIZE + "\n" +
            "precision highp float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform float uKernel[KERNEL_SIZE];\n" +
            "uniform vec2 uTexOffset[KERNEL_SIZE];\n" +
            "uniform float uColorAdjust;\n" +
            "void main() {\n" +
            "    int i = 0;\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "        for (i = 0; i < KERNEL_SIZE; i++) {\n" +
            "            vec4 texc = texture2D(sTexture, vTextureCoord + uTexOffset[i]);\n" +
            "            sum += texc * uKernel[i];\n" +
            "        }\n" +
            "    sum += uColorAdjust;\n" +
            "    gl_FragColor = sum;\n" +
            "}\n";    
}