/**
 * @author CŽline Bensoussan
 * A2 - Transform Hierarchy for Animated Characters
 * October 4, 2013
 */

package comp557.a2;

public class CharacterCreator {

	static public String name = "SKATER CARTER - Celine Bensoussan 260350861";
	
	static public DAGNode create() {
		
		//--------- COLORS --------//
		
		float[] white  = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] black  = { 0,0,0,1 };
		float[] purple = { 0.4f, 0.2f, 0.8f, 1.0f }; 
		float[] blue = {0.2f, 0.2f, 1.0f, 1.0f}; 
		float[] red = {1.0f, 0.0f, 0.0f, 1.0f};
		float[] skin = {1.0f, 0.8f, 0.6f, 1.0f};
		float[] orange = {1.0f, 0.4f, 0.2f, 1.0f};
		float[] brown = {0.8f, 0.6f, 0.2f,1.0f};
		float[] grey   = { 0.5f, 0.5f, 0.5f, 1.0f };
		
		//--------- ROOT --------//
		
		FreeJoint root = new FreeJoint("Body", 2, 4, red);
		
		//--------- HEAD --------//
		
		BallJoint neck = new BallJoint("Neck", 0.7, 1, 1, 1, 0, 2.2, 0, skin,
											-20, 20, -80, 80, -20, 20);
		root.add(neck);

		Cylinder head = new Cylinder("Head", 1.3, 2.5, 0, 1.5, 0, 90, 0, 0, skin);
		neck.add(head);

		Sphere eye1 = new Sphere("Right Eye", 0.3, 1, 2, 1, 0.4, 0.4, 1, white);
		head.add(eye1);

		Sphere eye2 = new Sphere("Left Eye", 0.3, 1, 2, 1, -0.4, 0.4, 1, white);
		head.add(eye2);

		Sphere pupil1 = new Sphere("Right Pupil", 0.18, 1, 1.5, 1, 0.05, 0, 0.2, black);
		eye1.add(pupil1);

		Sphere pupil2 = new Sphere("Left Pupil", 0.18, 1, 1.5, 1, -0.05, 0, 0.2, black);
		eye2.add(pupil2);
		
		Sphere nose = new Sphere("Nose", 0.3, 1.2, 1, 2, 0, -0.1, 1.2, skin);
		head.add(nose);

		Sphere base_hat = new Sphere("Hat", 1.3, 1, 0.9, 1, 0, 1.3, 0, purple);
		head.add(base_hat);
		
		Sphere hat = new Sphere("Hat", 1, 1.3, 0.2, 1.3, -0.4, 0, 0.8, orange);
		base_hat.add(hat);
		
		//--------- RIGHT ARM --------//
		
		BallJoint shoulderR = new BallJoint("Right Arm", 0.7, 1, 1, 1, 1.6, 1.5, 0.3, red,
											-100, 100, -90, 50, -70, 70);
		root.add(shoulderR);

		Cylinder armR = new Cylinder("Right Arm", 0.4, 2, 1.3, 0, 0, 90, 90, 0, red);
		shoulderR.add(armR);

		HingeJoint elbowR = new HingeJoint("Right Elbow", 0.5, 1, 1, 1, 1, 0, 0, red,
											"Y",  -160, 0);
		armR.add(elbowR);

		Cylinder foreArmR = new Cylinder("Right Fore Arm", 0.35, 2, 1, 0, 0, 90, 90, 0, red);
		elbowR.add(foreArmR);
		
		HingeJoint wristR = new HingeJoint("Right Wrist", 0.35, 1, 1, 1, 1.4, 0, 0, skin,
											"X", -90, 90);
		foreArmR.add(wristR);
		
		Sphere handR = new Sphere("Right Hand", 0.5, 1.3, 0.7, 0.7, 0, 0, 0, skin);
		wristR.add(handR);
		
		//--------- CIGARETTE --------//
		
		Cylinder cig1 = new Cylinder("Cigarrette", 0.05, 0.5, 0.4, 0, -0.3, 0, 0, 0, brown);
		handR.add(cig1);
		
		Cylinder cig2 = new Cylinder("Cigarrette", 0.05, 0.5, 0, 0, -0.5, 0, 0, 0, white);
		cig1.add(cig2);
		
		//--------- LEFT ARM --------/
				
		BallJoint shoulderL = new BallJoint("Left Arm", 0.7, 1, 1, 1, -1.6, 1.5, 0.3, red,
											-90, 90, -120, 120, -90, 90);
		root.add(shoulderL);

		Cylinder armL = new Cylinder("Left Arm", 0.4, 2, -1.3, 0, 0, 90, 90, 0, red);
		shoulderL.add(armL);

		HingeJoint elbowL = new HingeJoint("Left Elbow", 0.5, 1, 1, 1, -1, 0, 0, red,
											"Y", 0, 160);
		armL.add(elbowL);

		Cylinder foreArmL = new Cylinder("Left Fore Arm", 0.35, 2, -1, 0, 0, 90, 90, 0, red);
		elbowL.add(foreArmL);
		
		HingeJoint wristL = new HingeJoint("Left Wrist", 0.35, 1, 1, 1, -1.4, 0, 0, skin,
											"X", -90, 90);
		foreArmL.add(wristL);
		
		Sphere handL = new Sphere("Left Hand", 0.5, 1.3, 0.7, 0.7, 0, 0, 0, skin);
		wristL.add(handL);

		//---------  HIPS --------//
				
		HingeJoint hips = new HingeJoint("Hips", 2, 1, 1, 1, 0, -2, 0, blue,
										"Y", -30, 30);
		root.add(hips);
		
		BallJoint hipsR = new BallJoint("Right Hips", 0.3, 1, 1, 1, 1.2, -1, 0, blue,
										-40, 36, -20, 90, -5, 20);
		hips.add(hipsR);
		
		BallJoint hipsL = new BallJoint("Left Hips", 0.3, 1, 1, 1, -1.2, -1, 0, blue,
										-40, 36, -30, 90, -5, 20);
		hips.add(hipsL);
		
		//---------  RIGHT LEG --------//
		
		Cylinder thighR = new Cylinder("Right Thigh", 0.6, 4, 0, 0, 0, 90, 0, 0, blue);
		hipsR.add(thighR);
		
		HingeJoint kneeR = new HingeJoint("Right Knee", 0.65, 1, 1, 1, 0, -1.8, 0, blue,
											"X", 0, 90);
		thighR.add(kneeR);
		
		Cylinder legR = new Cylinder("Right Leg", 0.5, 2.5, 0, -1, 0, 90, 0, 0, blue);
		kneeR.add(legR);
		
		HingeJoint ankleR = new HingeJoint("Right Ankle", 0.5, 1, 1, 1, 0, -1.5, 0, skin,
											"Y", -30, 30);
		legR.add(ankleR);
		
		Sphere footR = new Sphere("Right Foot", 1, 0.7, 0.7, 1.3, 0, -0.5, 0.5, purple);
		ankleR.add(footR);
				
		//---------  LEFT LEG --------//
		
		Cylinder thighL = new Cylinder("Left Thigh", 0.6, 4, 0, 0, 0, 90, 0, 0, blue);
		hipsL.add(thighL);
		
		HingeJoint kneeL = new HingeJoint("Left Knee", 0.65, 1, 1, 1, 0, -1.8, 0, blue,
											"X", 0, 90);
		thighL.add(kneeL);
		
		Cylinder legL = new Cylinder("Left Leg", 0.5, 2.5, 0, -1, 0, 90, 0, 0, blue);
		kneeL.add(legL);
		
		HingeJoint ankleL = new HingeJoint("Left Ankle", 0.5, 1, 1, 1, 0, -1.5, 0, skin,
											"Y", -30, 30);
		legL.add(ankleL);
		
		Sphere footL = new Sphere("Left Foot", 1, 0.7, 0.7, 1.3, 0, -0.5, 0.5, purple);
		ankleL.add(footL);
		
		//---------  SKATEBOARD --------//
		
		Sphere skateboard = new Sphere("Skateboard", 2.5, 2, 0.1, 0.5, 1.5, -0.5, 0, grey);
		footL.add(skateboard);
		
		Cylinder bar1 = new Cylinder("Front Bar", 0.3, 2.5, 3, -0.25, 0, 0, 0, 0, grey);
		skateboard.add(bar1);
		
		Cylinder bar2 = new Cylinder("Back Bar", 0.3, 2.5, -3, -0.25, 0, 0, 0, 0, grey);
		skateboard.add(bar2);
				
		Sphere wheel1 = new Sphere("Skateboard", 0.4, 1, 1, 0.6, 0, 0, -1.2, white);
		bar1.add(wheel1);
		
		Sphere wheel2 = new Sphere("Skateboard", 0.4, 1, 1, 0.6, 0, 0, 1.2, white);
		bar1.add(wheel2);
		
		Sphere wheel3 = new Sphere("Skateboard", 0.4, 1, 1, 0.6, 0, 0, -1.2, white);
		bar2.add(wheel3);
		
		Sphere wheel4 = new Sphere("Skateboard", 0.4, 1, 1, 0.6, 0, 0, 1.2, white);
		bar2.add(wheel4);
    	
    	return root;
	}
}
