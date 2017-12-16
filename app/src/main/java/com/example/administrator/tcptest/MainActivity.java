package com.example.administrator.tcptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
{
    private Handler mMainHandler;
    private Socket socket;
    // 线程池
    private ExecutorService mThreadPool;

    InputStream is;
    InputStreamReader isr ;
    BufferedReader br ;

    private DataOutputStream dataOutputStream;
    private ByteArrayOutputStream byteArrayOutputStream;
    // 接收服务器发送过来的消息
    String response;
    OutputStream outputStream;

    private Button btnConnect, btnDisconnect, btnSend, btnSend_image, btnReceive_message;
    private TextView receive_message;
    private EditText mEdit;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化所有按钮
        btnConnect = (Button) findViewById(R.id.connect);
        btnDisconnect = (Button) findViewById(R.id.disconnect);
        btnSend = (Button) findViewById(R.id.send);
        mEdit = (EditText) findViewById(R.id.edit);
        receive_message = (TextView) findViewById(R.id.receive_message);
        btnReceive_message = (Button) findViewById(R.id.Receive);
        btnSend_image = (Button) findViewById(R.id.send_image);
        imageView = (ImageView) findViewById(R.id.my_image);


        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        receive_message.setText(response);
                        break;
                    case 1:
                        imageView.setImageBitmap((Bitmap)msg.obj);
                        break;
                }
            }
        };


        //创建客户端 & 服务器的连接
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("MainActivity", "onClick");
                            // 创建Socket对象 & 指定服务端的IP 及 端口号
                            socket = new Socket("0.tcp.ngrok.io", 10003);
                            // 判断客户端和服务器是否连接成功
                            if(socket.isConnected())
                                Log.d("MainActivity", "connected");
                            else
                                Log.d("MainActivity", "failed to connect");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally
                        {
                            Log.d("MainActivity", "ddd");
                        }
                    }
                });

            }
        });

        //接收 服务器消息
        btnReceive_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            is = socket.getInputStream();
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
                            response = br.readLine();
                            //通知主线程,将接收的消息显示到界面
                            Message msg = Message.obtain();//-------------------------------------?
                            msg.what = 0;
                            mMainHandler.sendMessage(msg);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

        btnSend_image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
//                        //从drawble里读图片
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
//                        options.outWidth = 48;
//                        options.outHeight = 64;
//                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img, options);

                        try
                        {
                            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("img.jpg"));
                            Log.d("MainActivity", "bitmap.getConfig(): " + bitmap.getConfig());
                            Log.d("MainActivity", "bitmap.getWidth(): " + bitmap.getWidth());
                            Log.d("MainActivity", "bitmap.getHeight(): " + bitmap.getHeight());
                            Log.d("MainActivity", "bitmap.getByteCount(): " + bitmap.getByteCount());
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = bitmap;
                            mMainHandler.sendMessage(msg);
                            dataOutputStream = new DataOutputStream(socket.getOutputStream());

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();

//                            //直接转bitmap为byteArray，不压缩
//                            int bytes = bitmap.getByteCount();
//                            ByteBuffer buffer = ByteBuffer.allocate(bytes);
//                            bitmap.copyPixelsToBuffer(buffer);
//                            byte[] byteArray = buffer.array();//-128~127

                            Log.d("MainActivity", "byteArray.length: " + byteArray.length);
                            int array_length = byteArray.length;
                            ByteBuffer b = ByteBuffer.allocate(4);
                            b.putInt(array_length);
                            dataOutputStream.write(b.array());
                            dataOutputStream.write(byteArray);
                            dataOutputStream.flush();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

//                        try {
//                            int imageSize=9216;//expected image size 64X48X3
//                            InputStream in = socket.getInputStream();
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            byte buffer[] = new byte[1024];
//                            int remainingBytes = imageSize; //
//                            while (remainingBytes > 0) {
//                                int bytesRead = in.read(buffer);
//                                if (bytesRead < 0) {
//                                    throw new IOException("Unexpected end of data");
//                                }
//                                baos.write(buffer, 0, bytesRead);
//                                remainingBytes -= bytesRead;
//                            }
//                            in.close();
//                            byte[] imageByte = baos.toByteArray();
//                            baos.close();
//                            //通知主线程,将接收的消息显示到界面
//                            int pixels[] = new int[imageSize/3];
//                            for(int i = 0; i < imageSize/3; i++) {
//                                int r = imageByte[3*i];
//                                int g = imageByte[3*i + 1];
//                                int b = imageByte[3*i + 2];
//                                if (r < 0)
//                                    r = r + 256; //Convert to positive
//                                if (g < 0)
//                                    g = g + 256; //Convert to positive
//                                if (b < 0)
//                                    b = b + 256; //Convert to positive
//                                pixels[i] = Color.rgb(b,g,r);
//                            }
//
//                            Bitmap bitmap = Bitmap.createBitmap(pixels, 48, 64, Bitmap.Config.ARGB_8888);
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                });

            }
        });


        //发送消息 给 服务器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            outputStream = socket.getOutputStream();
                            dataOutputStream = new DataOutputStream(outputStream);
                            //传字符串
                            outputStream.write((mEdit.getText().toString()+"\n").getBytes("utf-8"));

                            //传double
                            double compass = 2.333;
                            outputStream.write((Double.toString(compass)+"\n").getBytes("utf-8"));
                            // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                            //传int
                            int count = 4324235;
                            ByteBuffer b = ByteBuffer.allocate(4);
                            b.putInt(count);
                            byte[] byteArray = b.array();
                            Log.d("MainActivity", "byteArray.length: " + byteArray.length);
                            outputStream.write(byteArray);
                            outputStream.flush();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //断开客户端 & 服务器的连接
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.close();
                    br.close();
                    socket.close();
                    // 判断客户端和服务器是否已经断开连接
                    if(socket.isConnected())
                        Log.d("MainActivity", "failed to disconnect");
                    else
                        Log.d("MainActivity", "disconnect");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
