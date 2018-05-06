package infovis.diagram;

import infovis.diagram.elements.Element;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;



public class View extends JPanel{
	private Model model = null;
	private Color color = Color.BLUE;
	private double scale = 1;
	private double overviewDiagramScale;
	private double overviewBoxScale = 0.25;
	private int markerBoxStroke = 2;
	private double translateX=0;
	private double translateY=0;
	private double overviewTranslateX=0;
	private double overviewTranslateY=0;
	private Rectangle2D marker = new Rectangle2D.Double(overviewTranslateX,overviewTranslateY,0,0);
	private Rectangle2D overviewRect = new Rectangle2D.Double();   

	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}

	
	public void paint(Graphics g) {
		
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.clearRect(0, 0, getWidth(), getHeight());
		
		//main diagram
		g2D.translate(translateX,translateY);
		g2D.scale(scale,scale);
		paintDiagram(g2D);

		//reset translation and scale
		g2D.scale(1/scale,1/scale);
		g2D.translate(-translateX,-translateY);


		//set overview dimensions in full scale - keep size constant
		overviewRect = new Rectangle2D.Double(overviewTranslateX,overviewTranslateY,getWidth()*overviewBoxScale, getHeight()*overviewBoxScale);
		g2D.setColor(new Color(0xffcccccc));
		g2D.fill(overviewRect);
		g2D.setColor(new Color(0xff000000));
		g2D.draw(overviewRect);


		//calc scale factor for overview diagram
		double[] limits = getDiagramLimits();
		g2D.translate(overviewTranslateX, overviewTranslateY);
		overviewDiagramScale = Math.min((overviewRect.getWidth())/ limits[0],
				(overviewRect.getHeight())/ limits[1]);
		g2D.scale(overviewDiagramScale, overviewDiagramScale);
		paintDiagram(g2D);

		//reset scale and translate
		g2D.scale(1/ overviewDiagramScale,1/ overviewDiagramScale);
		g2D.translate(-overviewTranslateX, -overviewTranslateY);

		//create marker rectangle
		marker = new Rectangle2D.Double(marker.getX(),marker.getY(),getWidth()/scale* overviewDiagramScale,getHeight()/scale* overviewDiagramScale);
		g2D.setStroke(new BasicStroke(markerBoxStroke));
		g2D.setColor(new Color(0xffff0000));
		//adjust for size of stroke
		g2D.draw(new Rectangle2D.Double(marker.getX() - markerBoxStroke /2, marker.getY() - markerBoxStroke /2,
				marker.getWidth() + markerBoxStroke, marker.getHeight() + markerBoxStroke));

		
	}
	private void paintDiagram(Graphics2D g2D){
		for (Element element: model.getElements()){
			element.paint(g2D);
		}
	}

	private double[] getDiagramLimits(){

		double xLimit = 0;
		double yLimit = 0;
		for (Element element : model.getElements()){
			if (element.getX() > xLimit)
				xLimit = element.getX();
			if (element.getY() > xLimit)
				yLimit = element.getY();
		}

		//add offsets to give a space around diagram
		double offset = 50;
		xLimit += offset;
		yLimit += offset;

		double[] limits = {xLimit, yLimit};
		return limits;
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	public double getScale(){
		return scale;
	}
	public double getTranslateX() {
		return translateX;
	}
	public void setTranslateX(double translateX) {
		this.translateX = translateX;
	}
	public double getTranslateY() {
		return translateY;
	}
	public void setTranslateY(double tansslateY) {
		this.translateY = tansslateY;
	}

	public void updateTranslation(){
		//derive from marker position (relative to overview) - utilise bounds checking in updateMarker function
		setTranslateX(-(marker.getX()-overviewTranslateX) / overviewDiagramScale * scale);
		setTranslateY(-(marker.getY()-overviewTranslateY) / overviewDiagramScale * scale);
	}
	public void updateMarker(int x, int y){

//		System.out.println("updating marker position");

		//check position is valid
		if (overviewRect.contains(x, y, marker.getWidth(), marker.getHeight())){
			marker.setRect(x, y, marker.getWidth(), marker.getHeight());
		}
	}
	public void updateOverviewPosition(int x, int y){

//		System.out.println("updating overview position");

		//check validity
		if (new Rectangle2D.Double(0,0,getWidth(),getHeight())
				.contains(overviewTranslateX+x,overviewTranslateY+y,overviewRect.getWidth(),overviewRect.getHeight())){
			overviewTranslateX += x;
			overviewTranslateY += y;

			//update marker as well
			marker.setRect(marker.getX() + x, marker.getY() + y, marker.getWidth(), marker.getHeight());
		}
	}
	public Rectangle2D getMarker(){
		return marker;
	}
	public boolean markerContains(int x, int y){
		return marker.contains(x, y);
	}
	public boolean overviewContains(int x, int y) {return overviewRect.contains(x,y);}

}
 