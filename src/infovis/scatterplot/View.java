package infovis.scatterplot;

import infovis.debug.Debug;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.swing.JPanel;

public class View extends JPanel {
	     private Model model = null;

	     private Rectangle2D markerRectangle = new Rectangle2D.Double(0,0,0,0);
         private Rectangle2D matrixRectangle = new Rectangle2D.Double(0,0,0,0);
	     private boolean markerVisible = false;

        private int X_LABEL_SPACE;
        private int Y_LABEL_SPACE;
        private int PLOT_SIZE;
        private final double PLOT_OFFSET_FACTOR = 0.1;

        private Color DEF_POINT_COL = new Color(0xff000000);
        private Color LINKED_POINT_COL = new Color(0xffff0000);
        private Color BG_COLOR = new Color(0xffdddddd);


        public void setModel(Model model) {
        this.model = model;
        }
        public Rectangle2D getMarkerRectangle() {
                return markerRectangle;
            }
        public void setMarkerVisibility(boolean isVisible){markerVisible = isVisible;}

        public void setMarkerRectangle(Rectangle2D r) {

            // allow drawing left and upwards
            // adjust x and y to account for negative w an h
            if (r.getWidth() < 0){
                r = new Rectangle2D.Double(r.getX() + r.getWidth(), r.getY(),
                        markerRectangle.getWidth()-r.getWidth(), r.getHeight());
            }
            if (r.getHeight() < 0){
                r = new Rectangle2D.Double(r.getX(), r.getY() + r.getHeight(),
                        r.getWidth(), markerRectangle.getHeight()-r.getHeight());
            }

            //check marker is within matrix
            if (!matrixRectangle.contains(r))
                return;

            //check that marker stays within one cell
            int[] cell1 = cell_containing_point(r.getX(), r.getY());
            int[] cell2 = cell_containing_point(r.getX() + r.getWidth(), r.getY() + r.getHeight());
            if (cell1[0] != cell2[0]
                    || cell1[1] != cell2[1]){
                return;
            }

            //TODO consider extending rectangle to follow cursor even when outside cell

            //set rectangle
            markerRectangle = r;

            //mark necessary points
            brushAndLink(markerRectangle, cell1);

        }
		 
		@Override
		public void paint(Graphics g) {



            final int NUM_KEYS = model.getLabels().size();
            final int POINT_RADIUS = 2;
            X_LABEL_SPACE = (int)(getWidth() * 0.12);
            Y_LABEL_SPACE = (int)(getHeight() * 0.1);
            PLOT_SIZE = Math.min((getWidth() - X_LABEL_SPACE) / NUM_KEYS, (getHeight() - Y_LABEL_SPACE) / NUM_KEYS);
            matrixRectangle = new Rectangle2D.Double(X_LABEL_SPACE,Y_LABEL_SPACE,PLOT_SIZE * NUM_KEYS,PLOT_SIZE * NUM_KEYS);

            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            //labels
            Font font = new Font("Serif", Font.PLAIN, Y_LABEL_SPACE/5);
            g2D.setFont(font);
            ArrayList<String> labels = model.getLabels();
            for (int i = 0; i < NUM_KEYS; i++){
                String label = labels.get(i);
                //x label
                g2D.drawString(label, X_LABEL_SPACE + (i*PLOT_SIZE), (int)(Y_LABEL_SPACE * 0.9));

                //y label
                g2D.drawString(label, (int)(X_LABEL_SPACE * 0.1), (int)(Y_LABEL_SPACE + ((i+0.5)*PLOT_SIZE)));

            }

            font = new Font("Serif", Font.PLAIN, (int)(Y_LABEL_SPACE/2));
            g2D.setFont(font);
            g2D.drawString("Scatter Plot Matrix", (int)(getWidth() * 0.3), (int)(getHeight()* 0.06));

            //frame
            for (int x = 0; x < NUM_KEYS; x++){
                for (int y = 0; y < NUM_KEYS; y++) {

                    if (x==y)
                        g2D.setColor(new Color(0xffbbbbbb));
                    else
                        g2D.setColor(BG_COLOR);


                    g2D.fillRect(X_LABEL_SPACE + (x*PLOT_SIZE), Y_LABEL_SPACE + (y*PLOT_SIZE),
                            PLOT_SIZE, PLOT_SIZE);
                    g2D.setColor(new Color(0xff000000));
                    g2D.drawRect(X_LABEL_SPACE + (x*PLOT_SIZE), Y_LABEL_SPACE + (y*PLOT_SIZE),
                            PLOT_SIZE, PLOT_SIZE);
                }
            }

            //for each data point
            for (Data d : model.getList()){

                g2D.setColor(d.getColor());

                //for each label on x and y axis
                for (int x = 0; x < NUM_KEYS; x++) {
                    for (int y = 0; y < NUM_KEYS; y++) {

                        //calculate offset for each graph
                        int plot_offset_x = X_LABEL_SPACE + (x * PLOT_SIZE);//to left of plot
                        int plot_offset_y = Y_LABEL_SPACE + ((y+1) * PLOT_SIZE);//to bottom of plot
                        //get data values
                        double x_val = d.getValues()[x];
                        double y_val = d.getValues()[y];
                        //calc correct position
                        double final_x = plot_offset_x + (getPointOffsetForRange(x_val,  model.getRanges().get(x)) * PLOT_SIZE);
                        double final_y = plot_offset_y - (getPointOffsetForRange(y_val,  model.getRanges().get(y)) * PLOT_SIZE);
                        //draw
                        g2D.fill(new Ellipse2D.Double(final_x - POINT_RADIUS, final_y - POINT_RADIUS,
                                2*POINT_RADIUS, 2*POINT_RADIUS));

                    }
                }

            }

            //render marker rectangle
            if (markerVisible){
                g2D.setColor(new Color(0xff0099ff));
                g2D.draw(markerRectangle);
            }

		}

		//changes the colour attribute of data points contained within a rectangle
		private void brushAndLink(Rectangle2D marker, int[] cell_coords){

            //get accepted ranges for points
            double minX,maxX,minY,maxY;
            double[] minPoint = getDataValuesForAbsPoint(marker.getX(),
                    marker.getY() + marker.getHeight(), cell_coords[0], cell_coords[1]);
            double[] maxPoint = getDataValuesForAbsPoint(marker.getX() + marker.getWidth(),
                    marker.getY(), cell_coords[0], cell_coords[1]);

            //compare all data to those ranges
            for (Data d : model.getList()){

                double data_x = d.getValues()[cell_coords[0]];
                double data_y = d.getValues()[cell_coords[1]];

                //check x dimension
                if (data_x < minPoint[0] || data_x > maxPoint[0]){
                    //out of range - set non highlighted
                    d.setColor(DEF_POINT_COL);
                    continue;
                }

                if (data_y < minPoint[1] || data_y > maxPoint[1]){
                    //out of range - set non highlighted
                    d.setColor(DEF_POINT_COL);
                    continue;
                }

                //otherwise - in range, set highlighted
                d.setColor(LINKED_POINT_COL);

            }

        }

		//returns a number between 0 and 1 describing the points position on an axis
		private double getPointOffsetForRange(double point, Range r){
		    if (point < r.getMin()){
		        System.err.println("point outside of range");
		        return 0.0;
            }
            if(point > r.getMax()){
                System.err.println("point outside of range");
                return 1.0;
            }

            double range = r.getMax() - r.getMin();
            range += (range * PLOT_OFFSET_FACTOR * 2.0);//add offsets above and below range
            if (range == 0.0)//protect against division by 0
                return 0.0;

            return (point - r.getMin() + (PLOT_OFFSET_FACTOR*range))/range;

        }

        //returns the data values on the correct scale for a point on the scatter matrix
        //takes cell as arguments as they have already been calculated in setMarkerRect
        private double[] getDataValuesForAbsPoint(double x,double y, int cell_x, int cell_y){

		    //get relative position within cell
            double rel_x = x - X_LABEL_SPACE - (cell_x * PLOT_SIZE);//in px
            rel_x /= PLOT_SIZE;//normalised

            double rel_y = ((cell_y+1) * PLOT_SIZE  +  Y_LABEL_SPACE) - y;//in px
            rel_y /= PLOT_SIZE;//normalised

            //adjust for plot offset
            double scale_factor = 1.0 / (1.0 - (PLOT_OFFSET_FACTOR*2));
            rel_x -= PLOT_OFFSET_FACTOR;
            rel_x *= scale_factor;
            rel_y -= PLOT_OFFSET_FACTOR;
            rel_y *= scale_factor;

            ArrayList<Range> ranges = model.getRanges();
            double[] rtn_values = new double[]{0.0,0.0};

            //x
            Range x_range = ranges.get(cell_x);
            rtn_values[0] = x_range.getMin() + ((x_range.getMax() - x_range.getMin()) * rel_x);
            //y
            Range y_range = ranges.get(cell_y);
            rtn_values[1] = y_range.getMin() + ((y_range.getMax() - y_range.getMin()) * rel_y);

            return rtn_values;
        }

        //returns which cell the given point is in, as x and y co-ordinates
        private int[] cell_containing_point(double x, double y){

		    if (!matrixRectangle.contains(x,y))
		        return new int[]{-1,-1};

		    int[] cell_coords = new int[2];

            cell_coords[0] = (int)((x - X_LABEL_SPACE) / PLOT_SIZE);
            cell_coords[1] = (int)((y - Y_LABEL_SPACE) / PLOT_SIZE);

            return cell_coords;
        }


}
