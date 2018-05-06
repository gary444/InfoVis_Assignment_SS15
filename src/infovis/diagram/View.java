package infovis.diagram;

import infovis.diagram.elements.Element;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.JPanel;



public class View extends JPanel{
	private Model model = null;
	private Color color = Color.BLUE;
	private double scale = 1;
	private double translateX= 0;
	private double translateY=0;
	private Rectangle2D marker = new Rectangle2D.Double(0,0,0,0);
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
		
		//set scale
		g2D.scale(scale,scale);
		
		paintDiagram(g2D);


		//set overview dimensions in full scale - keep size constant
		g2D.scale(1/scale,1/scale);
		overviewRect = new Rectangle2D.Float(0,0,getWidth() * 0.25f, getHeight()*0.25f);
		g2D.setColor(new Color(0xffcccccc));
		g2D.fill(overviewRect);
		g2D.setColor(new Color(0xff000000));
		g2D.draw(overviewRect);


		//calc scale factor for overview diagram
		double[] limits = getDiagramLimits();
		double ovw_offset = 20;
		double ovw_scale = Math.min((overviewRect.getWidth() - ovw_offset)/ limits[0],
				(overviewRect.getHeight() - ovw_offset)/ limits[1]);
		g2D.scale(ovw_scale,ovw_scale);
		paintDiagram(g2D);

		//draw marker rectangle
		marker = new Rectangle2D.Double(marker.getX(),marker.getY(),getWidth()/scale,getHeight()/scale);
//		g2D.scale(1 / scale, 1/scale);
		g2D.setStroke(new BasicStroke(10));
		g2D.setColor(new Color(0xffff0000));
		g2D.draw(marker);

		
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
	public void updateTranslation(double x, double y){
		setTranslateX(x);
		setTranslateY(y);
	}	
	public void updateMarker(int x, int y){
		marker.setRect(x, y, marker.getWidth(), marker.getHeight());
	}
	public Rectangle2D getMarker(){
		return marker;
	}
	public boolean markerContains(int x, int y){
		return marker.contains(x, y);
	}
}
 