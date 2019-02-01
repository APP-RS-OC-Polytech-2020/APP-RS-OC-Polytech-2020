import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class Window extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CameraGrabber cam;
	private Thread camThread=null;
	private boolean camIsRunning;
	
	private JFrame frame;
	private JButton button;
	private MyCanvas canvas;
	
	public Window(){
		init();
	}
	public final void init(){
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		System.out.println("Open CV Processor Loaded");
		
		this.frame=new JFrame();
		this.frame.addWindowListener(new WindowAdapter(){
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent arg0){
				if(camIsRunning){
					camIsRunning=false;
					camThread.stop();
					camThread=null;
				}
				cam.Close();
			}
		});
		this.frame.setVisible(true);
		this.frame.setBounds(100, 100, 757, 361);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().setLayout(null);
		
		this.canvas=new MyCanvas();
		
		this.cam=new CameraGrabber(0,60);
		this.camIsRunning=false;
		
		this.button=new JButton("Start");
		this.button.setBounds(408,11,90,75);
		this.button.addActionListener(new ActionListener(){
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e){
				if (camThread == null) {
					camThread = new Thread() {
						private int absoluteBodySize;
						private Object bodyCascade;

						public void run() {
							Mat matImg;
							while (true) {
								matImg = cam.Capture();
								
								MatOfRect bodies = new MatOfRect();
								Mat grayFrame = new Mat();
								
								// convert the frame in gray scale
								Imgproc.cvtColor(matImg, grayFrame, Imgproc.COLOR_BGR2GRAY);
								// equalize the frame histogram to improve the result
								Imgproc.equalizeHist(grayFrame, grayFrame);
								
								// compute minimum body size (20% of the frame height, in our case)
								if (this.absoluteBodySize == 0)
								{
									int height = grayFrame.rows();
									if (Math.round(height * 0.2f) > 0)
									{
										this.absoluteBodySize = Math.round(height * 0.2f);
									}
								}
								
								// detect full bodies
								this.bodyCascade=new CascadeClassifier("/home/pi/opencv/opencv-4.0.0/data/haarcascades/haarcascade_lowerbody.xml");
								((CascadeClassifier) this.bodyCascade).detectMultiScale(grayFrame, bodies, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteBodySize, this.absoluteBodySize), new Size());
										
								// draw rectangles
								Rect[] bodiesArray = bodies.toArray();
								for (int i = 0; i < bodiesArray.length; i++)
									Imgproc.rectangle(matImg, bodiesArray[i].tl(), bodiesArray[i].br(), new Scalar(0, 255, 0), 3);
								
								//possibility to detect other parts of the body : upperbody, lowerbody, face
								
								
								BufferedImage bufImg = new BufferedImage(matImg.cols(), matImg.rows(), BufferedImage.TYPE_3BYTE_BGR);
								matImg.get(0, 0, ((DataBufferByte)bufImg.getRaster().getDataBuffer()).getData());
								
								
								
							
								BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufImg)));
								
								
								
								Result qrCodeResult;
								try {
									qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
									
									//Phase test voir ce que donne points
									float scaleFactor=2;
									ResultPoint[] points = qrCodeResult.getResultPoints();
									System.out.println("Info Image totale:  Largeur : "+ bufImg.getWidth() + " Longueur : " + bufImg.getHeight());
									for (int i = 0; i < points.length; i++) {
										System.out.println(points[i]);
									}
									if(points.length>3){
										Point topleft = new Point(points[1].getX(), points[1].getY());
										Point bottomRight = new Point(points[3].getX(), points[3].getY());
										Imgproc.rectangle(matImg, topleft, bottomRight, new Scalar(0, 255, 0), 3);
										bufImg = new BufferedImage(matImg.cols(), matImg.rows(), BufferedImage.TYPE_3BYTE_BGR);
										matImg.get(0, 0, ((DataBufferByte)bufImg.getRaster().getDataBuffer()).getData());
									}
									System.out.println(qrCodeResult.getText());
								} catch (NotFoundException e1) {
									//e1.printStackTrace();
								}
								
								
								canvas.setImage(bufImg);														
								canvas.repaint();
								
								//System.out.println("FPS: "+cam.getFps());
									
								try {
									Thread.sleep(cam.CameraGetFps());
								} catch (Exception e) {
										
								}
							}
						};
					};
				}
				if (camIsRunning == false) {
					camIsRunning = true;
					camThread.start();
					button.setText("Stop");
				} else {
					camThread.stop();
					button.setText("Start");
					camIsRunning = false;
					camThread = null;
				}	
			}
		});
		
		this.frame.getContentPane().add(canvas);
		this.frame.getContentPane().add(button);
	}
	private class MyCanvas extends Canvas{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private BufferedImage img = null;

	      public MyCanvas () {
	         setBackground (Color.GRAY);
	         setBounds(10, 10, 387, 279);
	      }

	      public void paint (Graphics g) {
	         Graphics2D g2;
	         g2 = (Graphics2D) g;

	         if (img != null) {
	        	 g2.drawImage( img, 0, 0, getWidth(), getHeight(), null); 
	         }
	         //g2.drawString ("This is is a string", 0 + 10, getHeight() - 10);
	      }
	      public void repaint() {
	    	  	super.repaint();	  
	      }
	      
	      public void setImage(BufferedImage img) {
	    	  this.img = img;
	      }
	}
	
}
