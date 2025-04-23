package world.junseo.co.kr.easyscreenrecord.glescore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BasePolygon {
	//중점의 (x,y,z)좌표를 저장할 변수
	//반겸을 저장할 변수
	private float cx, cy, cz, r;
	private int sides; // 다각형의 면의 수
	
	// 좌표 배열 : (x,y)정점들
	private float[] xarray = null;
	private float[] yarray = null;
	
	// 텍스쳐 배열 : (x,y) -> 흔히 (s,t)로도 표현한다.
	private float[] sarray = null;
	private float[] tarray = null;
	
	//private static final float sizeRate = 0.8181818F;
	//private static final float sizeRate = 0.5625F;	// 정상
	private float sizeRateW = 1F;
	private float sizeRateH = 1F;
	
	// 생성자
	public BasePolygon(float incx, float incy, float incz, float inr, int insides, float cameraWidth, float cameraHeight){
		cx = incx;
		cy = incy;
		cz = incz;
		r = inr;
		sides = insides;
		
		//카메라 해상도에 따른 정다각형 출력에 맞춘 변환 비율 설정
		if(cameraWidth > cameraHeight)
			sizeRateH =  cameraHeight/cameraWidth;
		else
			sizeRateW = cameraWidth/cameraHeight;
		
		//배열들에 메모리 할당
		xarray = new float[sides];
		yarray = new float[sides];
		
		//텍스처 점 배열에 메모리 할당
		sarray = new float[sides];
		tarray = new float[sides];
		
		//정점들을 계산
		calcArrays();
		
		//텍스처 점들을 계산
		calcTextureArrays();
	}
	
	
	// 정점 촤표들을 기점과 반경을 이용해 구한 후 변환
	// 각도에 대한 실제 로직은 getMultiplierArray()에서 구현
	private void calcArrays() {
		//원의 반경이 1이고 기점 0위치에 있다는 전제로 정점들을 구함
		float[] xmarry = this.getXMultiplierArray();
		float[] ymarray = this.getYMultiplierArray();
		
		// xarray 계산 : 기점의 X부분
		// 좌표와 반경의 곱에 더해서 정점을 구함
		for(int i = 0; i < sides; i++){
			float curm = xmarry[i];
			float xcoord = cx + r * curm * sizeRateH;
			//float xcoord = cx + r*curm;
			xarray[i] = xcoord;
		}		
		//this.printArray(xarray, "xarray");
		
		// yarray 계산 : y좌표에 대해 마찬가지로 수행
		for(int i = 0; i < sides; i++) {
			float curm = ymarray[i];
			float ycoord = cy + r * curm * sizeRateW;
			yarray[i] = ycoord;
		}
		//this.printArray(yarray, "yarray");
	}
	
	// 텍스처 배열 계산, 거의 비슷한 방법이지만 다각형이 정사각 공간에 매핑되어야 한다.
	private void calcTextureArrays() {
		float[] xmarray = this.getXMultiplierArray();
		float[] ymarray = this.getYMultiplierArray();
		
		// xarray 계산
		for(int i = 0; i < sides; i++){
			float curm = xmarray[i];
			float xcoord = 0.5f + 0.5f * curm * sizeRateH;
			//float xcoord = 0.5f + 0.5f * curm;
			sarray[i] = xcoord;
		}
		//this.printArray(sarray, "sarray");
		
		// yarray 계산
		for(int i = 0; i < sides; i++){
			float curm = ymarray[i];
			float ycoord = 0.5f + 0.5f * curm * sizeRateW;
			tarray[i]  = ycoord;
		}
		//this.printArray(tarray, "tarray");			
	}
	
	// 정점들의 자바 배열을 nio부동 소수점 버퍼로 변환
	public FloatBuffer getVertexBuffer() {
		int vertices = sides + 1;
		int coordinates = 3; // 사용할 정점의 좌표계
		int floatsize = 4; // 버퍼 개체의 크기
		int spacePerVertex = coordinates * floatsize;
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(spacePerVertex * vertices);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer mFVertexBuffer = vbb.asFloatBuffer();
		
		// 첫번째 좌표 (x,y,z:0,0,0)을 버퍼에 추가
		mFVertexBuffer.put(cx);
		mFVertexBuffer.put(cy);
		mFVertexBuffer.put(cz);
		
		int totalPuts = 3;
		for(int i = 0; i < sides; i++){
			mFVertexBuffer.put(xarray[i]);
			mFVertexBuffer.put(yarray[i]);
			mFVertexBuffer.put(0.0f);
			totalPuts += 3;
		}
		//Log.d("버퍼에 추가된 총 좌표 수 : ", Integer.toString(totalPuts));
		return mFVertexBuffer;
	}
	
	// 텍스처 버퍼를 nio 버퍼로 변환
	public FloatBuffer getTextureBuffer(){
		int vertices = sides + 1;
		int coordinates = 2;
		int floatsize = 4;
		int spacePerVertex = coordinates * floatsize;
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(spacePerVertex * vertices);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer mFTextureBuffer = vbb.asFloatBuffer();
		
		// 첫 번째 좌표 (s,t:0,0)을 버퍼에 추가
		mFTextureBuffer.put(0.5f);
		mFTextureBuffer.put(0.5f);
		
		int totalPuts = 2;
		for(int i = 0; i < sides; i++) {
			mFTextureBuffer.put(sarray[i]);
			mFTextureBuffer.put(tarray[i]);
			totalPuts += 2;
		}
		//Log.d("버퍼에 추가된 총 텍스처 좌표 수 : ", Integer.toString(totalPuts));
		return mFTextureBuffer;
	}
	
	// 여러 개의 감삭혛을 구서하는 인덱스들을 계산, 종점 0에서 시작해서 0,1,2 0,2,3처럼 시계 방향으로 셈
	public ShortBuffer getIndexBuffer() {
		short[] iarray = new short[sides*3];
		ByteBuffer ibb = ByteBuffer.allocateDirect(sides * 3 * 2);
		ibb.order(ByteOrder.nativeOrder());
		ShortBuffer mIndexBuffer = ibb.asShortBuffer();
		for(int i = 0; i < sides; i++){
			short index1 = 0;
			short index2 = (short)(i+1);
			short index3 = (short)(i+2);
			if(index3 == sides + 1) {
				index3 = 1;
			}
			mIndexBuffer.put(index1);
			mIndexBuffer.put(index2);
			mIndexBuffer.put(index3);
			
			iarray[i*3 + 0] = index1;
			iarray[i*3 + 1] = index2;
			iarray[i*3 + 2] = index3;
		}
		//this.printShortArray(iarray, "index array");
		return mIndexBuffer;
	}
	
	// 각 정점별 각도 배열을 받아서 x축의 투영 승수를 계산
	private float[] getXMultiplierArray() {
		float[] angleArray = getAngleArrays();
		float[] xmultiplierArray = new float[sides];
		for(int i = 0; i < angleArray.length; i++){
			float curAngle = angleArray[i];
			float sinvalue = (float) Math.cos(Math.toRadians(curAngle));
			float absSinValue = Math.abs(sinvalue);
			if(isXPositiveQuadrant(curAngle)){
				sinvalue = absSinValue;
			} else {
				sinvalue = -absSinValue;
			}
			xmultiplierArray[i] = this.getApproxValue(sinvalue);
		}
		//this.printArray(xmultiplierArray, "xmultiplierArray");
		return xmultiplierArray;
	}
	
	// y축의 투영 승수를 계산
	private float[] getYMultiplierArray() {
		float[] angleArray = getAngleArrays();
		float[] ymultiplierArray = new float[sides];
		for(int i = 0; i < angleArray.length; i++){
			float curAngle = angleArray[i];
			float sinvalue = (float) Math.sin(Math.toRadians(curAngle));
			float absSinValue = Math.abs(sinvalue);
			if(isYPositiveQuadrant(curAngle)){
				sinvalue = -absSinValue;
			} else {
				sinvalue = absSinValue;
			}
			ymultiplierArray[i] = this.getApproxValue(sinvalue);
		}
		//this.printArray(ymultiplierArray,"ymultiplierArray");
		return ymultiplierArray;
	}
	
	private boolean isXPositiveQuadrant(float angle){
		if((0 <= angle) && (angle <= 90)){return true;}
		if((angle < 0) && (angle >= -90)){return true;}
		return false;
	}
	
	private boolean isYPositiveQuadrant(float angle){
		if((0 <= angle)&&(angle <= 90)){return true;}
		if((angle < 180)&&(angle >= 90)){return true;}
		return false;
	}
	
	// 중점과 각 정점을 잇는 각 직선별 각도 계싼
	private float[] getAngleArrays() {
		float[] angleArray = new float[sides];
		float commonAngle = 360.0f/sides;
		float halfAngle = commonAngle/2.0f;
		float firstAngle = 360.0f - (90+halfAngle);
		angleArray[0] = firstAngle;
		
		float curAngle = firstAngle;
		for(int i = 1; i < sides; i++){
			float newAngle = curAngle - commonAngle;
			angleArray[i] = newAngle;
			curAngle = newAngle;
		}
		//printArray(angleArray, "angleArray");
		return angleArray;
	}

	// 필요한 경우 약간의 곡선 처리
	private float getApproxValue(float f){
		return (Math.abs(f)<0.001)?0:f;
	}
	
	// 주어진 변의 수에 대해, 필요한 인덱스 수 반환 이것은 다각형 그리기에 필요한
	// 삼각형 수에 3을 곱하면 된다.
	public int getNumberOfIndeices(){
		return sides * 3;
	}
	
//	private void printArray(float[] array, String tag){
//		StringBuilder sb = new StringBuilder(tag);
//		for(int i = 0; i < array.length;i++){
//			sb.append(";").append(array[i]);
//		}
//		//Log.d("hh", sb.toString());
//	}
	
//	private void printShortArray(short array[], String tag){
//		StringBuilder sb = new StringBuilder(tag);
//		for(int i = 0; i < array.length; i++){
//			sb.append(";").append(array[i]);
//		}
//		//Log.d(tag, sb.toString());
//	}
}
