//Tyler Hughes
//COSC 4730
//Program 5 / BattleBots

package edu.cs4730.TCPclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

/*
 * this is a simple network client example.  Note it assumes you are using the emulators, but will work
 * on phones as well.  You just need to know the IP address.
 * 
 */

public class TCPclient extends Activity implements Button.OnClickListener{
    /** Called when the activity is first created. */
    TextView output;
    Button  mkconn;
    EditText hostname, port;
    Thread myNet;

    ImageView theboardfield;
    Bitmap boardbmp;
    Canvas boardcanv;
    ScrollView sv;
    int boardsize = 800;
    protected Handler imghandler;
    Paint myColor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        output = (TextView) findViewById(R.id.output);
        output.append("\n");
        hostname = (EditText) findViewById(R.id.EThostname);
        hostname.setText("10.0.2.2"); //This address is the localhost for the computer the emulator is running on.
        port = (EditText) findViewById(R.id.ETport);
        mkconn = (Button) findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);

        sv = (ScrollView)findViewById(R.id.scrollView);
        sv.post(new Runnable() {

            @Override
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        });

        theboardfield = (ImageView) findViewById(R.id.fireBoard);
        boardbmp = Bitmap.createBitmap(boardsize, boardsize, Bitmap.Config.ARGB_8888);
        boardcanv = new Canvas(boardbmp);
        boardcanv.drawColor(Color.WHITE);  //background color for the board.
        theboardfield.setImageBitmap(boardbmp);
        myColor = new Paint();  //default black
        myColor.setColor(Color.BLACK);
        myColor.setStyle(Paint.Style.FILL);
        myColor.setStrokeWidth(6);
        boardcanv.drawLine((boardsize / 2), 0, (boardsize / 2), boardsize, myColor);
        boardcanv.drawLine(0, (boardsize / 2), boardsize, (boardsize / 2), myColor);



        theboardfield.setOnTouchListener(new FiringTouchListener());
    }
    

	@Override
	public void onClick(View v) {
		doNetwork stuff = new doNetwork();
		myNet = new Thread(stuff);
		myNet.start();
		
		//An example of how you would write from here via the thread.  Note,
		//this will likely force close here, because the connection is not fully made at this point.
		//the thread just started.
		//stuff.out.println("hi there.");

	}
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	output.append(msg.getData().getString("msg"));
        }

    };
    public void mkmsg(String str) {
		//handler junk, because thread can't update screen!
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
	    handler.sendMessage(msg);
    }

    class FiringTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            // Retrieve the new x and y touch positions
            int x = (int) event.getX();
            int y = (int) event.getY();

            //calculate angle from center
            //fire if available, don't if not.

            return false;
        }

    }

    /*
     * this code does most of the work in a thread, so that it doesn't lock up the main (UI) thread
     * It call mkmsg (which calls the handler to update the screen)
     */
    class doNetwork  implements Runnable {
    	public PrintWriter out;
    	public  BufferedReader in;
    	 
    	public void run() {

            boolean going = true;
            int xmove = 1, ymove = 1, movecount = 50, currmove = 0;
    		 
        
        int p = Integer.parseInt(port.getText().toString());
        String h = hostname.getText().toString();
		mkmsg("host is " + h +"\n");
		mkmsg(" Port is " + p + "\n");
		try {
            InetAddress serverAddr = InetAddress.getByName(h);
            mkmsg("Attempt Connecting..." + h +"\n");
            Socket socket = new Socket(serverAddr, p);
            String message = "Hello from Client android emulator";

            //made connection, setup the read (in) and write (out)
            out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            Random rng = new Random();
            //now send a message to the server and then read back the response.
                try {
                 //write a message to the server
                 mkmsg("Attempting to send message ...\n");                 
                 out.println("RandomHamster 1 1 3");
                 mkmsg("Message sent...\n");
                    out.println("noop");
                    //wait for a status message...
                    try{ Thread.sleep(100); }catch(InterruptedException e){ }
                    String str = in.readLine();
                    mkmsg("rcv: " + str + "\n");
                    mkmsg("Scanning...\n");
                    out.println("scan");
                    //wait for a status message...
                    try{ Thread.sleep(100); }catch(InterruptedException e){ }
                    str = in.readLine();
                    mkmsg("rcv: " + str + "\n");
                    mkmsg("Moving "+ xmove + " " + ymove + "...\n");
                    out.println("move " + xmove + " " + ymove);
                    //wait for a status message...
                    try{ Thread.sleep(100); }catch(InterruptedException e){ }
                    int angle = rng.nextInt(361);
                    str = in.readLine();
                    mkmsg("rcv: " + str + "\n");
                    mkmsg("Firing at " + angle + "...\n");
                    out.println("fire " + angle);

                    while (going)
                    {
                        //try{ Thread.sleep(100); }catch(InterruptedException e){ }
                        //mkmsg("<Reading in Loop>\n");
                        //try to get the most recent message in the buffer...
                        //for (String line = in.readLine(); line.equals(str) == false; line = in.readLine())
                        //{
//                            mkmsg("looprcv: " + line + "\n");
//                            str = line;
//                        }
                        str = in.readLine();
                        mkmsg("rcv: " + str + "\n");
                        if (str.contains("Status"))
                        {
                            String[] data = str.split(" ");
                            mkmsg("<Got a Status, fire: " + data[4] + " move: " + data[3] + ">\n");
                            if (data[4].equalsIgnoreCase("0"))
                            {
                                //fire!
                                angle = rng.nextInt(361);
                                mkmsg("Firing at " + angle + "...\n");
                                out.println("fire " + angle);
                            }
                            else if (data[3].equalsIgnoreCase("0"))
                            {
                                if (currmove > movecount)
                                {
                                    xmove = rng.nextInt(3) - 1;
                                    ymove = rng.nextInt(3) - 1;
                                    while (xmove == 0 && ymove == 0)
                                    {
                                        xmove = rng.nextInt(3) - 1;
                                        ymove = rng.nextInt(3) - 1;
                                    }
                                    currmove = 0;
                                }
                                mkmsg("Moving "+ xmove + " " + ymove + "...\n");
                                out.println("move " + xmove + " " + ymove);
                                currmove++;
                            }
                        }
                        else
                        {
                            //check for endgame message here and change going accordingly
                            mkmsg("Scanning...\n");
                            out.println("scan");
                            continue;
                        }
                    }

                 mkmsg("Closing connection\n");
              } catch(Exception e) {
                  mkmsg("Error happened sending/receiving\n");

              } finally {
            	  in.close();
            	  out.close();
                  socket.close();
              }

          } catch (Exception e) {
      		mkmsg("Unable to connect...\n");
          } 
       }
    }
}