package com.comcast.viper.ebp.analyzer;


import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import javax.swing.*;

import javax.swing.border.TitledBorder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/*
import com.comcast.ccp.mpegts.es.PES;
import com.comcast.ccp.mpegts.es.PTSDTSIndicator;
import org.apache.commons.codec.binary.Hex;
*/


import com.comcast.ccp.mpegts.ts.AdaptationField;
import com.comcast.ccp.mpegts.ts.EncoderBoundaryPoint;
import com.comcast.ccp.mpegts.ts.Packet;
import com.comcast.ccp.mpegts.utils.PacketUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class SwingControlDemo {

    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private static final String nicAddress="eth5";
    BufferedWriter bw = null;
    public EncoderBoundaryPoint ebp;
    public int port;
    public List<Integer>fragNo = new ArrayList<Integer>();
    public List<String> fragNTP = new ArrayList<String>();
    public List<String> ebpHEX = new ArrayList<String>();    
    public List<String> fragPTS = new ArrayList<String>();
    private  boolean runThread = true;
    //private EBPAnalyzer ebpa;

          
    public static final int timeout=600;
    

    public SwingControlDemo(){
        prepareGUI();
    }

    public static void main(String[] args){
        SwingControlDemo swingControlDemo = new SwingControlDemo();
        swingControlDemo.showTextFieldDemo();
    }

    private void prepareGUI(){
        mainFrame = new JFrame("Meta Data Analyzer");
        mainFrame.setSize(1000,500);
        mainFrame.setLayout(new GridLayout(0, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });

        statusLabel = new JLabel("",JLabel.LEFT);

        statusLabel.setSize(350,100);
      //  mainFrame.setLayout(new GridLayout(3,1));

        controlPanel = new JPanel();
        controlPanel.setLayout(null);


        mainFrame.add(controlPanel);
        mainFrame.add(statusLabel);
        mainFrame.setVisible(true);

    }

    private void showTextFieldDemo(){



        JLabel  namelabel= new JLabel("Multicast_IP: ", JLabel.LEFT);
        JLabel  sourcelabel = new JLabel("Source IP: ", JLabel.CENTER);
        JLabel  portlabel = new JLabel("Ports: ", JLabel.CENTER);

        final JTextField userText = new JTextField(6);
        final JTextField sourceText  = new JTextField(6);
        final JTextField portsText  = new JTextField(6);
        

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                String data = userText.getText();
                String data1 = sourceText.getText();
                String data2 = portsText.getText();
                statusLabel.setText(data);
                statusLabel.setText(data1);
                statusLabel.setText(data2);
                System.out.println("UDP Transmission Starts...!");
                try
                {
                    NetworkInterface nic = NetworkInterface.getByName(nicAddress);

                    DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
                    channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.valueOf(true));
                    channel.bind(new InetSocketAddress(Integer.parseInt(data2)));
                    channel.configureBlocking(true);

                    if(nic.isUp())
                    {
                        System.out.println("NIC IP is up.."+ nic.getDisplayName());
                    }
                    else
                    {
                        System.out.println("NIC IP is not up.." +nic.getDisplayName());
                    }
                    channel.join(InetAddress.getByName(data), nic, InetAddress.getByName(data1));

                    boolean flag=InetAddress.getByName(data).isReachable(timeout);
                    System.out.println(flag);
                    ByteBuffer buffer = ByteBuffer.allocate(((Integer)channel.getOption(StandardSocketOptions.SO_RCVBUF)).intValue());
                    //  ByteBuffer bufftemp = buffer.duplicate();
					byte[] tsPacket = new byte[188];
					
					buffer.clear();
					channel.receive(buffer);
					buffer.flip();
					byte[] ipPacket = new byte[buffer.remaining()];
					buffer.get(ipPacket, 0, ipPacket.length);
					try{
						for (int i = 0; i < ipPacket.length; i += 188)
						{
							System.arraycopy(ipPacket, i, tsPacket, 0, 188);
							if (PacketUtils.hasAdaptationField(tsPacket))
							{
								 
								Packet packet = new Packet(tsPacket);
								AdaptationField af = packet.getAdaptationField();
								ebp = af.getEncoderBoundaryPoint();

							}
						}	
					}catch(ArrayIndexOutOfBoundsException ex){
						ex.printStackTrace();
					}
				}catch(IOException ex){
					ex.printStackTrace();
				}
            }
        });
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                String data = userText.getText();
                String data1 = sourceText.getText();
                String data2 = portsText.getText();
                statusLabel.setText(data);
                statusLabel.setText(data1);
                statusLabel.setText(data2);
            }
        });

        controlPanel.add(namelabel);
        controlPanel.add(userText);
        controlPanel.add(sourcelabel);
        controlPanel.add(sourceText);
        controlPanel.add(portlabel);
        controlPanel.add(portsText);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        namelabel.setBounds(0, 50, 100, 20);
        userText.setBounds(100, 50, 100, 20);
        sourcelabel.setBounds(200, 50, 100, 20);
        sourceText.setBounds(310, 50, 100, 20);
        portlabel.setBounds(410, 50, 100, 20);
        portsText.setBounds(510, 50, 100, 20);
        startButton.setBounds(650,50,100,20);
        stopButton.setBounds(850,50,100,20);
        mainFrame.setVisible(true);
    }
}