import javax.swing.*;
import java.awt.*;
// For button click handling
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// ArrayList implementation
import java.util.List;
import java.util.ArrayList;
// Mouse event handling
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;   
// Slider change handling
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
  holds all the classes and physics used in this program it extends jpanel so that the panel can have a constructor 
  different from what is normally provided by JPanel, actionlistener is implemented to use checkboxes and sliders
*/
public class bb extends JPanel implements ActionListener {
    // set a starting color and size of the ball
    Color currentColor = Color.CYAN;
    double currentSize = 50.0;
   // List storing all balls that have been created
    List<Ball> balls = new ArrayList<>();
    Timer timer;
    static boolean spacemode=false; /*has a spacemode where the earths gravity is neglected and gravitation bw
    objects drives the sim*/
    static double wind; //pushes balls sideways
    /*
      Inner class representing a single ball.stores coordinates,velocity,mass,size,forces acting on it and color
      although the constructor only holds the coordinates,size,color and mass
    */
    class Ball { 
        double x, y;   
        double vx=0,vy=0;   
        double mass;
        double fx=0,fy=0;
        double size;      
        Color color;      
        // Constructor to initialize a ball
        Ball(double x, double y, double size, Color color,double mass) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.mass=size;
        }
        //assigns the forces that are acting upon the ball
        void forcecalc(double forcex,double forcey){
            this.fx+=forcex;
            this.fy+=forcey; 
        }
    }
        /*
           this is the constructor that the panel follows, it consists of a mouselistener that deploys balls at the 
           coordinates of the cursor at that very instant, it creates a ball and gets its color from the colorchooser
           dialog,it also has a timer that refreshes the simulation every 16ms keeping the simulation updated 
           NOTE- the timer is initialised only in the first run of the bb() after this bb() only runs when
           mouse is pressed and that causes only the mouselistener code to run in bb()
        */
    bb() {
        this.addMouseListener(new MouseAdapter() {
            // Called whenever a mouse button is pressed
            public void mousePressed(MouseEvent e) {
                // Create a new ball at mouse position
                Ball newBall = new Ball(e.getX(),e.getY(),currentSize,currentColor,currentSize);
                // Add the new ball to the list
                balls.add(newBall);
                //repaint so that the ball uploads instantly
                repaint();
            }
        });
        timer=new Timer(20,this);
        timer.start();
    }
    /*
     this is the main site of all the physics after timer is run it immediately calls teh actionperformed()
     (this is part of timer implementation)
     every 16ms it calls actionperformed to calculate all the forces and stuff
    */
       @Override
public void actionPerformed(ActionEvent e) {
    for (Ball b : balls) {
        b.fx = 0;
        b.fy = 0;
        // Apply Environment Forces
        if (!spacemode) {
            double gravity = 0.5;//to make it look realistic but the balls settle too fast
            double weight = b.mass * gravity;
            b.forcecalc(wind, weight);//call the forcecalc() to assign the particular forces acting on the ball
        } 
        else {
            b.forcecalc(wind, 0);
        }
        // Move the ball (Velocity + Position)
        // We do this BEFORE collision checks to see where they end up
        double ax = b.fx / b.mass;
        double ay = b.fy / b.mass;
        b.vx += ax;
        b.vy += ay;
        b.x += b.vx;
        b.y += b.vy;

        // Wall Collisions (Keep them inside window)
        if (b.y > getHeight() - b.size / 2) {
            b.y = getHeight() - b.size / 2;
            b.vy = -b.vy * 0.8; // Floor friction
        }
        if (b.y < 0) { // Ceiling
            b.y = 0;
            b.vy = -b.vy * 0.8;
        }
        if (b.x < 0 || b.x > getWidth()) { // Walls
            b.vx = -b.vx * 0.8;
            // Push back inside to prevent sticking
            if (b.x < 0) b.x = 0;
            if (b.x > getWidth()) b.x = getWidth();
        }
    }
    //main spacemode code, iterates over every ball calculating distance and corresponding gravitation forces
    for (int i = 0; i < balls.size(); i++) {
        for (int j = i + 1; j < balls.size(); j++) {
            Ball b1 = balls.get(i);
            Ball b2 = balls.get(j);
            double dx = b1.x - b2.x;
            double dy = b1.y - b2.y;
            double distsq = dx * dx + dy * dy;
            double dist = Math.sqrt(distsq);

            if (dist == 0){// avoids blackhole effect
                dist = 0.01;
            } 

            if (dist < (b1.size + b2.size) / 2) { //ball to ball collision 
                double nx = dx / dist;
                double ny = dy / dist; //sin cos components 
                double overlap = (b1.size / 2 + b2.size / 2) - dist;
                if (overlap > 0) { //code so that the balls dont get absorbed into eachother 
                    b1.x += overlap * nx * 0.5; //pushes ball 1/2 of number of pixels in overlap outward
                    b1.y += overlap * ny * 0.5; //pushes ball 1/2 of number of pixels in overlap above
                    b2.x -= overlap * nx * 0.5; // outward but other direction
                    b2.y -= overlap * ny * 0.5;
                }
                double dvx = b1.vx - b2.vx; //change in velocity in x
                double dvy = b1.vy - b2.vy; //change in velocity in y
                double velocityAlongNormal = dvx * nx + dvy * ny; //used to judge whether balls are moving towards eachother or away
                if (velocityAlongNormal < 0) { //if balls are moving towards eachother
                    
                    double restitution = 0.8; //e
                    double impulse = -(1+restitution) * velocityAlongNormal; //impulse physics
                    impulse /= (1 / b1.mass + 1 / b2.mass);
                    double impulseX = impulse * nx;
                    double impulseY = impulse * ny;
                    b1.vx += impulseX / b1.mass;
                    b1.vy += impulseY / b1.mass;
                    b2.vx -= impulseX / b2.mass;
                    b2.vy -= impulseY / b2.mass;
                }
            }
            if (spacemode && dist > (b1.size + b2.size) / 2) { //used to calculate gravitation forces 
                double G = 5.0;
                double force = (G * b1.mass * b2.mass) / (dist * dist);
                double forceX = force * (dx / dist);
                double forceY = force * (dy / dist);
                b2.vx += forceX / b2.mass;
                b2.vy += forceY / b2.mass;
                b1.vx -= forceX / b1.mass;
                b1.vy -= forceY / b1.mass;
            }
        }
    }
    repaint(); //used to immediately bring changes calls the paintcomponent method
}
    @Override
    public void paintComponent(Graphics g) {
        // Clears the panel before repainting
        super.paintComponent(g);
        // Set background color to black
        this.setBackground(new Color(0, 0, 0));
        // Draw each ball in the list
        for (Ball b : balls) {
            // Sets the new color of the ball
            g.setColor(b.color);
            // Convert size (diameter) to radius
            int radius = (int) (b.size / 2);
            /*Draw filled circle centered at (x, y) the b.x-readius and b.y-radius is used 
              to make the center of the ball at the position of the cursor 
            */ 
            g.fillOval((int) (b.x - radius),(int) (b.y - radius),(int) b.size,(int) b.size);
        }
    }
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("bouncyball sim");
        // Set window size
        frame.setSize(2000, 2000);
        // Exit program when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create the drawing panel
        bb panel = new bb();
        // Create a control panel for UI elements
        JPanel controlpanel = new JPanel();
        controlpanel.setBackground(Color.GRAY);
        // Button to open color chooser
        JButton cbtn = new JButton("pick a color");
        // Action listener for color picker button
        cbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open color chooser dialog
                Color newCol = JColorChooser.showDialog(frame,"choose a color",panel.currentColor);
                if (newCol != null) {
                    panel.currentColor = newCol;   
                    cbtn.setBackground(newCol);    
                }
            }
        });
        JSlider sizeS = new JSlider(10, 150, 50);
        // Listen for slider value changes
        sizeS.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                panel.currentSize = sizeS.getValue();
            }
        });
        JPanel windpanel=new JPanel();
        windpanel.setBackground(Color.GRAY);
        JSlider windS=new JSlider(JSlider.VERTICAL,0,100,50);
        windS.setPreferredSize(new Dimension(30,300));
        windS.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                double rawval=windS.getValue();
                wind=(rawval-50)/100;
            }
        });
        JCheckBox spacemodeCheckBox = new JCheckBox("space gravity");
        spacemodeCheckBox.setBackground(Color.GRAY);
        spacemodeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                spacemode = spacemodeCheckBox.isSelected();
            }
        });
        controlpanel.add(cbtn);
        controlpanel.add(new JLabel("Size"));
        controlpanel.add(sizeS);
        controlpanel.add(spacemodeCheckBox);
        windpanel.setPreferredSize(new Dimension(100,0));
        windpanel.setLayout(new GridBagLayout());
        windpanel.add(new JLabel("wind speed"));
        windpanel.setForeground(new Color(0,0,0));
        windpanel.add(windS);
        frame.add(controlpanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(windpanel,BorderLayout.EAST);
        frame.setVisible(true);
    }
}


