/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.comcast.viper.ebp.analyzer;

import com.comcast.ccp.mpegts.es.PES;
import com.comcast.ccp.mpegts.es.PTSDTSIndicator;
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
import org.apache.commons.codec.binary.Hex;

public class EBPReader extends Thread {
    private String multicastAddress,sourceIpAddresss,nicName;
    public EncoderBoundaryPoint ebp;
    public int port;
    public List<Integer>fragNo = new ArrayList<Integer>();
    public List<String> fragNTP = new ArrayList<String>();
    public List<String> ebpHEX = new ArrayList<String>();    
    public List<String> fragPTS = new ArrayList<String>();
    private  boolean runThread = true;
    private EBPAnalyzer ebpa;
    public final static byte SYNC_BYTE = (byte) 0x47;
    EBPReader(String multicastAddress, String sourceIpAddresss , int port, String nicName){

        this.multicastAddress = multicastAddress ;
        this.sourceIpAddresss = sourceIpAddresss ;
        this.port = port;
        this.nicName = nicName;

    }
    public  void setrunThread(boolean run){
        this.runThread = run;
    }
    @Override
    public void run() {
            BigInteger biPrev = null ;
            String ebpDataPrev = null;
            int frag = 0;
            int coNo = 0;
            int ptsColNo = 0;
            String fNTP,fNTPPrev ;
            String multicastAddress = this.multicastAddress;
            String sourceIpAddresss = this.sourceIpAddresss;
        //    fNTPPrev = null;
            int port = this.port;
            String nicName = this.nicName;
            System.out.println("\n------------------STARTING TESTIN FOR PORT - " + port + "---------------------------");
            EBPAnalyzer.jTextArea2.append("\n------------------STARTING TESTIN FOR PORT - " + port + "---------------------------");
            NetworkInterface nic = null;
            try
            {
              nic = NetworkInterface.getByName(nicName);
            }
            catch (Exception ex)
            {
              System.out.println("Cannot find NIC " + nicName);
              EBPAnalyzer.jTextArea2.append("\nCannot find NIC " + nicName);
              System.exit(-1);
            }
            try{
                DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.valueOf(true));
                channel.bind(new InetSocketAddress(port));
                channel.configureBlocking(true);
                channel.join(InetAddress.getByName(multicastAddress), nic, InetAddress.getByName(sourceIpAddresss));
                ByteBuffer buffer = ByteBuffer.allocate(((Integer)channel.getOption(StandardSocketOptions.SO_RCVBUF)).intValue());
              //  ByteBuffer bufftemp = buffer.duplicate();
                byte[] tsPacket = new byte['¼'];
                for (;;)
                {
                /*  coNo = getColumnByName(ebpa.jTable1, Integer.toString(port));
                  ptsColNo = getColumnByName(ebpa.jTable2, Integer.toString(port));
                    */
                  if (!runThread){
                      System.out.println("\n----------------STOPPING TEST FOR - " + port +" -------------------");
                      EBPAnalyzer.jTextArea2.append("\n----------------STOPPING TEST FOR - " + port +" -------------------");
                      this.fragNTP.clear();
                      this.fragNo.clear();
                      this.fragPTS.clear();
                      this.port = 0;
                      break;
                  }
                  buffer.clear();
                  channel.receive(buffer);
                  buffer.flip();
                  byte[] ipPacket = new byte[buffer.remaining()];
                  buffer.get(ipPacket, 0, ipPacket.length);
             //     byte b = bufftemp.get();

             /*    if (b != SYNC_BYTE) {
                    System.out.println("Not a sync byte : " + b);
                   // throw new IllegalStateException("Not a sync byte: " + b);
                 }else{
                     System.out.println("Sync Byte");
                 }*/
                 // buffer.get(ipPacket, 0, ipPacket.length);
                    for (int i = 0; i < ipPacket.length; i += 188)
                        {
                            System.arraycopy(ipPacket, i, tsPacket, 0, 188);
                            if (PacketUtils.hasAdaptationField(tsPacket))
                            {
                              EBPAnalyzer.jTextArea2.append("\nAdoptation Fiel Found");
                              Packet packet = new Packet(tsPacket);
                              AdaptationField af = packet.getAdaptationField();
                              ebp = af.getEncoderBoundaryPoint();

                              if (ebp != null)
                                  
                                {
                                    
                                    //System.out.println("--------------------" + port + "---------------------------");
                                    fragNo.add(frag);
                                    //EBPAnalyzer.jTextArea1.append("-----------" + port + "------------\n" + ebp.toString() + "\n");
                                  // System.out.println(ebp.toString());
                                    EBPAnalyzer.jTextArea2.append("\n--------------------" + port +" EBP NTP TIME : " + SimpleDateFormat.getDateTimeInstance().format(ebp.getEncBoundaryPointTimeNtp().getTime()) + "\n");
                                    //  EBPAnalyzer.jTextArea1.append( "PORT : " + port + " -> EBP NTP TIME : " + SimpleDateFormat.getDateTimeInstance().format(ebp.getEncBoundaryPointTimeNtp().getTime()) + "\n");
                                    fNTP = SimpleDateFormat.getDateTimeInstance().format(ebp.getEncBoundaryPointTimeNtp().getTime());
                                    fragNTP.add(fNTP);
                                    //System.out.println("fragNo = " + frag +" NTP = " + fNTP);
                                     //System.out.println(ebp.toString());            
                                    long difference = Math.abs(ebp.getEncBoundaryPointTimeNtp().getTimeInMillis() - System.currentTimeMillis());
                                    if (PacketUtils.isPESPacket(tsPacket)) 
                                    {
                                        PES p = new PES(tsPacket);
                                        if (p.getPtsDtsIndicator().getCode() == PTSDTSIndicator.ONLY_PTS
                                                        .getCode()
                                                        || p.getPtsDtsIndicator().getCode() == PTSDTSIndicator.BOTH
                                                                        .getCode()) {
                                            BigInteger bi = p.getPTS();
                                            if (bi != null){								
                                                 //   System.out.println("PTS == "+bi );
                                                 //   EBPAnalyzer.jTextArea1.append("PORT : " + port + " -> PTS port  = " + bi + "\n");
                                                 fragPTS.add(""+bi);
                                                 //   PTS_table.setValueAt(bi, frag, ptsColNo);
                                            }else{
                                                // PTS_table.setValueAt("NULL PTS", frag, ptsColNo);
                                                 fragPTS.add("NULL PTS");
                                            }
                                            if (difference > 10000L) {
                                                 ebpHEX.add(Hex.encodeHexString(tsPacket));
                                            }                                       
                                        }
                                    }
                                    frag = frag + 1;
                                }
                            }else{
                                  EBPAnalyzer.jTextArea2.append("\nNot an Adoptation Field");
                            }
                        }
                } 
            } catch (IOException ex){
                ex.printStackTrace();
            }
    }
    public static void hexConv(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        System.out.println(output);
    }
}
