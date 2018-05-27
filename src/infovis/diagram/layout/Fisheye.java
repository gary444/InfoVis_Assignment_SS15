package infovis.diagram.layout;

import infovis.debug.Debug;
import infovis.diagram.Model;
import infovis.diagram.View;
import infovis.diagram.elements.Edge;
import infovis.diagram.elements.Element;
import infovis.diagram.elements.Vertex;

import java.util.Iterator;

import static java.lang.StrictMath.sqrt;

/*
 * 
 */

public class Fisheye implements Layout{

	Model transformed_model;
	Model original_model;

	private int focusX;
	private int focusY;

	public Fisheye(int _focusX, int _focusY){
		this.focusX = _focusX;
		this.focusY = _focusY;
	}

	public void setMouseCoords(int x, int y, View view) {

		focusX = x;
		focusY = y;

		return;
	}

	public Model transform(Model model, View view) {
		original_model = model;

		transformModel(view);

		return transformed_model;
	}

	private void transformModel(View view){

		transformed_model = new Model();

		//distortion factor
		final double d = 2;

		//find extremes of dimensions in x and y in order to scale to full window
		double limits[] = getDiagramLimits();
		double windowScaleFactor = Math.min(view.getWidth() / limits[0] ,view.getHeight() / limits[1]);

		// derive positions of transformed vertices from positions of original dataset
		for (Vertex v : original_model.getVertices()){

			double normX = v.getX() * windowScaleFactor;
			double normY = v.getY() * windowScaleFactor;
			double maxDistX, maxDistY;
			double normDistX, normDistY;
			double newX, newY;

			if (normX > focusX)
				maxDistX = view.getWidth() - focusX;
			else
				maxDistX = -focusX;

			if (normY > focusY)
				maxDistY = view.getHeight() - focusY;
			else
				maxDistY = -focusY;

			normDistX = normX-focusX;
			normDistY = normY-focusY;

			newX = focusX + maxDistX * fisheyeMagnification(normDistX/maxDistX, d);
			newY = focusY + maxDistY * fisheyeMagnification(normDistY/maxDistY, d);

			Vertex nv = new Vertex(newX, newY);


//			calculate position of nearest edge to centre
			double QnormX, QnormY;
			if (normX>focusX){
				QnormX = normDistX - (v.getWidth()/2);
			}
			else{
				QnormX = normDistX + (v.getWidth()/2);

			}
			if (normY>focusY){
				QnormY = normDistY - (v.getHeight()/2);
			}
			else{
				QnormY = normDistY + (v.getHeight()/2);
			}
			double QfishX = focusX + maxDistX * fisheyeMagnification(QnormX/maxDistX, d);
			double QfishY = focusY + maxDistY * fisheyeMagnification(QnormY/maxDistY, d);

			double newWidth = sqrt(2)*Math.abs(QfishX-newX);
			double newHeight = sqrt(2)*Math.abs(QfishY-newY);


			double ratio = v.getWidth()/v.getHeight();
			if (newWidth/v.getWidth() < newHeight/v.getHeight()) {
				nv.setWidth(newWidth);
				nv.setHeight(nv.getWidth() / ratio);
			}
			else {
				nv.setHeight(newHeight);
				nv.setWidth(nv.getHeight()*ratio);
			}

			//OFFSET centre for new size?



			transformed_model.addVertex(nv);
		}
	}

	private double fisheyeMagnification(double inputValue,double distortion){
		return ((distortion + 1)*inputValue) / ((distortion*inputValue) + 1);
	}

	private double[] getDiagramLimits(){
		double xLimit = 0;
		double yLimit = 0;
		for (Element element : original_model.getElements()){
			if (element.getX() > xLimit)
				xLimit = element.getX();
			if (element.getY() > yLimit)
				yLimit = element.getY();
		}
		//add offsets to give a space around diagram
		double offset = 50;
		xLimit += offset;
		yLimit += offset;

		double[] limits = {xLimit, yLimit};
		return limits;
	}
}

