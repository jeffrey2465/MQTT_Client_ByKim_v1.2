package kr.ac.hywoman;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class MQTT_Client_ByKim extends JFrame {
	
	//#. Broker 서버 정보 필드
	String mqtt_server;	// = "tcp://test.mosquitto.org";
	String client_id;	// = "Input your ClientID";
	String username;	// = "Input your ssl_username";	
	String passwd;		// = "Input your ssl_passwd";	
	String topicOfpub;	// = "/dhkim"; // 반드시 "/ + topic"
	String topicOfsub;	// = "/dhkim"; // 반드시 "/ + topic"
	MyMQTT mqtt;

	//#. GUI Component
	private JPanel contentPane;
	private JTextField broker_id;
	private JTextField broker_port;
	private JTextField send_msg;
	private JTextField topic_sub;
	private JTextField topic_pub;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rdbtn_private;
	private JRadioButton rdbtn_public;
	private JComboBox cb_broker_url;
	private JButton btnConnect;
	private JButton btn_topic_sub;
	private JButton btn_topic_pub;
	private JTextArea ta_sub;
	private JTextArea ta_pub;
	private JScrollPane scrollPane_pub;
	private JScrollPane scrollPane_sub;
	private JButton btn_send;
	
	
	/**
	 * JFrame 자동 추가 
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MQTT_Client_ByKim frame = new MQTT_Client_ByKim();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MQTT_Client_ByKim() {
		setResizable(false);
		
		//#Title
		this.setTitle("MQTT Client By DHKIM v1.2");
		String path = System.getProperty("user.dir");
		this.setIconImage(new ImageIcon(path + "/resources/icon.png").getImage());		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 783, 366);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		cb_broker_url = new JComboBox();
		cb_broker_url.setBackground(Color.WHITE);
		cb_broker_url.setModel(new DefaultComboBoxModel(new String[] {"test.mosquitto.org", "broker.emqx.io"}));
		cb_broker_url.setBounds(46, 226, 205, 21);
		contentPane.add(cb_broker_url);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(34, 189, 241, 2);
		contentPane.add(separator);
		
		JLabel lblNewLabel = new JLabel("Server Setting");
		lblNewLabel.setFont(new Font("Consolas", Font.BOLD, 15));
		lblNewLabel.setBounds(81, 10, 128, 15);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("IP");
		lblNewLabel_1.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(47, 136, 57, 15);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Port");
		lblNewLabel_2.setFont(new Font("Consolas", Font.PLAIN, 12));
		lblNewLabel_2.setBounds(47, 161, 57, 15);
		contentPane.add(lblNewLabel_2);
		
		broker_id = new JTextField();
		broker_id.setText("127.0.0.1");
		broker_id.setBounds(83, 133, 169, 21);
		contentPane.add(broker_id);
		broker_id.setColumns(10);
		
		broker_port = new JTextField();
		broker_port.setText("1883");
		broker_port.setBounds(83, 158, 169, 21);
		contentPane.add(broker_port);
		broker_port.setColumns(10);
		
		rdbtn_private = new JRadioButton("private broker");				
		rdbtn_private.setBackground(Color.WHITE);
		rdbtn_private.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				broker_id.setEnabled(true);
				broker_port.setEnabled(true);
				cb_broker_url.setEnabled(false);
			}
		});
		buttonGroup.add(rdbtn_private);
		rdbtn_private.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtn_private.setBounds(68, 45, 141, 23);
		contentPane.add(rdbtn_private);
		
		rdbtn_public = new JRadioButton("public  broker");
		rdbtn_public.setBackground(Color.WHITE);
		rdbtn_public.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				broker_id.setEnabled(false);
				broker_port.setEnabled(false);
				cb_broker_url.setEnabled(true);
			}
		});
		rdbtn_public.setSelected(true);//처음 설정값 private으로 설정
		buttonGroup.add(rdbtn_public);
		rdbtn_public.setFont(new Font("Consolas", Font.PLAIN, 12));
		rdbtn_public.setBounds(68, 70, 141, 23);
		contentPane.add(rdbtn_public);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(34, 99, 234, 2);
		contentPane.add(separator_1);
		
		JLabel lblNewLabel_3 = new JLabel("public broker");
		lblNewLabel_3.setFont(new Font("Consolas", Font.BOLD, 12));
		lblNewLabel_3.setBounds(45, 201, 110, 15);
		contentPane.add(lblNewLabel_3);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(34, 257, 241, 2);
		contentPane.add(separator_2);
		
		JLabel lblPrivateBroker = new JLabel("private broker");
		lblPrivateBroker.setFont(new Font("Consolas", Font.BOLD, 12));
		lblPrivateBroker.setBounds(46, 111, 110, 15);
		contentPane.add(lblPrivateBroker);
		
		JLabel lblNewLabel_4 = new JLabel("copyright(c) 2021. dhkim. All rights reserved");
		lblNewLabel_4.setForeground(Color.BLUE);
		lblNewLabel_4.setFont(new Font("굴림", Font.ITALIC, 12));
		lblNewLabel_4.setBounds(497, 312, 270, 15);
		contentPane.add(lblNewLabel_4);
		
		JLabel lblPublication = new JLabel("Publish");
		lblPublication.setHorizontalAlignment(SwingConstants.CENTER);
		lblPublication.setFont(new Font("Consolas", Font.BOLD, 15));
		lblPublication.setBounds(304, 10, 212, 15);
		contentPane.add(lblPublication);
		
		JLabel lblSubscription = new JLabel("Subscribe");
		lblSubscription.setHorizontalAlignment(SwingConstants.CENTER);
		lblSubscription.setFont(new Font("Consolas", Font.BOLD, 15));
		lblSubscription.setBounds(534, 9, 213, 15);
		contentPane.add(lblSubscription);
		
		scrollPane_sub = new JScrollPane();
		scrollPane_sub.setBounds(536, 55, 212, 203);
		contentPane.add(scrollPane_sub);
		
		ta_sub = new JTextArea();
		ta_sub.setEditable(false);
		scrollPane_sub.setViewportView(ta_sub);
		
		scrollPane_pub = new JScrollPane();
		scrollPane_pub.setBounds(304, 57, 212, 203);
		contentPane.add(scrollPane_pub);
		
		ta_pub = new JTextArea();
		ta_pub.setEditable(false);
		scrollPane_pub.setViewportView(ta_pub);
		
		send_msg = new JTextField();
		send_msg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = send_msg.getText();
				if(!msg.equals("")) {
					mqtt.publish(topicOfpub, msg, 0);
					send_msg.setText("");
					String time = (new SimpleDateFormat( "yy/MM/dd HH:mm:ss" )).format( Calendar.getInstance().getTime());				
					ta_pub.append(String.format("[%s] - published \n  %s", time, msg)+"\n");
					scroll(scrollPane_sub,SwingConstants.BOTTOM);
				}
			}
		});
		send_msg.setEnabled(false);
		send_msg.setBounds(304, 269, 141, 21);
		contentPane.add(send_msg);
		send_msg.setColumns(10);
		
		btn_send = new JButton("Send");
		btn_send.setBackground(Color.BLACK);
		btn_send.setForeground(Color.WHITE);
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = send_msg.getText();
				if(!msg.equals("")) {
					mqtt.publish(topicOfpub, msg, 0);
					send_msg.setText("");
					String time = (new SimpleDateFormat( "yy/MM/dd HH:mm:ss" )).format( Calendar.getInstance().getTime());				
					ta_pub.append(String.format("[%s] - published \n  %s", time, msg)+"\n");
					scroll(scrollPane_sub,SwingConstants.BOTTOM);
				}
			}
		});
		btn_send.setEnabled(true);
		btn_send.setFont(new Font("Consolas", Font.PLAIN, 12));
		btn_send.setBounds(447, 269, 69, 23);
		contentPane.add(btn_send);
		
		JLabel label = new JLabel("Topic");
		label.setFont(new Font("Consolas", Font.BOLD, 12));
		label.setBounds(536, 35, 43, 15);
		contentPane.add(label);
		
		topic_sub = new JTextField();
		topic_sub.setColumns(10);
		topic_sub.setBounds(575, 32, 100, 21);
		contentPane.add(topic_sub);
		
		btn_topic_sub = new JButton("set");
		btn_topic_sub.setForeground(Color.WHITE);
		btn_topic_sub.setBackground(Color.BLACK);
		btn_topic_sub.setFont(new Font("Consolas", Font.BOLD, 12));
		btn_topic_sub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//publish의 경우 한번 연결된 서버에 topic을 바꾸어 전송가능
				//subscribe는 연결을 끊고 다시 subscribe해야 함 (안그러면 그동안의 모든 topic을 모두 구분없이 수신)				
				if(btn_topic_sub.getText().equals("set")) {
					mqtt.init();
					String str = topic_sub.getText();
					if(str!=null) {
						topicOfsub=topic_sub.getText();
						topic_sub.setEnabled(false);
						mqtt.subscribe(topicOfsub, 0);						
						btn_topic_sub.setText("reset");
					} else {
						JOptionPane.showMessageDialog(MQTT_Client_ByKim.this, "Topic을 입력해주세요");
					}
				} else {					
					mqtt.disconnect();
					topic_sub.setEnabled(true);
					btn_topic_sub.setText("set");
				}
			}
		});
		btn_topic_sub.setBounds(678, 31, 69, 23);
		contentPane.add(btn_topic_sub);
		
		JButton btn_clear_sub = new JButton("clearSub");	
		btn_clear_sub.setBackground(Color.BLACK);
		btn_clear_sub.setForeground(Color.WHITE);
		btn_clear_sub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ta_sub.setText("");
			}
		});
		btn_clear_sub.setFont(new Font("Consolas", Font.BOLD, 12));
		btn_clear_sub.setBounds(647, 269, 100, 23);
		contentPane.add(btn_clear_sub);
		
		JButton btn_clear_pub = new JButton("clearPub");
		btn_clear_pub.setForeground(Color.WHITE);
		btn_clear_pub.setBackground(Color.BLACK);
		btn_clear_pub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ta_pub.setText("");
			}
		});
		btn_clear_pub.setFont(new Font("Consolas", Font.BOLD, 12));
		btn_clear_pub.setBounds(540, 269, 100, 23);
		contentPane.add(btn_clear_pub);
		
		JLabel label_1 = new JLabel("Topic");
		label_1.setFont(new Font("Consolas", Font.BOLD, 12));
		label_1.setBounds(305, 36, 43, 15);
		contentPane.add(label_1);
		
		topic_pub = new JTextField();
		topic_pub.setColumns(10);
		topic_pub.setBounds(344, 33, 101, 21);
		contentPane.add(topic_pub);
		
		btn_topic_pub = new JButton("set");
		btn_topic_pub.setForeground(Color.WHITE);
		btn_topic_pub.setBackground(Color.BLACK);
		btn_topic_pub.setFont(new Font("Consolas", Font.BOLD, 12));
		btn_topic_pub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btn_topic_pub.getText().equals("set")) {
					String str = topic_pub.getText();
					if(str!=null) {
						topicOfpub=topic_pub.getText();
						topic_pub.setEnabled(false);
						send_msg.setEnabled(true);
						btn_send.setEnabled(true);
						
						btn_topic_pub.setText("reset");
					} else {
						JOptionPane.showMessageDialog(MQTT_Client_ByKim.this, "Topic을 입력해주세요");
					}
				} else {
					send_msg.setEnabled(false);
					btn_send.setEnabled(false);
					topic_pub.setEnabled(true);
					btn_topic_pub.setText("set");
				}
				
			}
		});
		
		btn_topic_pub.setBounds(447, 32, 69, 23);
		contentPane.add(btn_topic_pub);
		
		btnConnect = new JButton("Connect");
		btnConnect.setForeground(Color.WHITE);
		btnConnect.setBackground(Color.BLACK);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				
				//#1. 선택 broker 정보 가져오기
				if(rdbtn_private.isSelected()) { //private broker
					String ip = broker_id.getText();
					String port = broker_port.getText();
					mqtt_server = "tcp://"+ip+":"+port;
				} else { //public broker
					String ip = (String)cb_broker_url.getSelectedItem();
					mqtt_server = "tcp://"+ip+":1883";
				}
				
				
				//mqtt_server = "tcp://test.mosquitto.org:1883";
				client_id=MqttAsyncClient.generateClientId(); //client_id가 서로 달라야 동일 TCP 포트로 다중접속 (멀티플랙싱) 가능
				username="username";
				passwd="passwd";				
				System.out.println(btnConnect.getText());
				
				
				//#2. 
				if(btnConnect.getText().equals("Connect")) {
					mqtt = new MyMQTT(mqtt_server, client_id, username, passwd); //username, passwd는 의미없음
					mqtt.init();
					topicOfsub = topicOfpub = "/TopicForConnectionTest"; //초기 연결을 확인하기 위해서 동일한 토픽으로 pub/sub하여 데이터 도착여부 확인
					mqtt.subscribe(topicOfsub, 0);
					try { Thread.sleep(1000); } catch (InterruptedException e1) {}
					mqtt.publish(topicOfpub, "ConnectionTest", 0);
					//btnConnect.setText("DisConnect");
				}else {
					mqtt.disconnect();
					serverConnectedViewSetting(false);
					//btnConnect.setText("Connect");
				}
			}
		});
		btnConnect.setFont(new Font("Consolas", Font.BOLD, 12));
		btnConnect.setBounds(46, 268, 205, 23);
		contentPane.add(btnConnect);
		
		//초기 View Enable 세팅
		serverConnectedViewSetting(false); //false 미연결 상태
	}
	
	//Connect/DisConnect 이후 버튼들 Enable/disEnable
	void serverConnectedViewSetting(boolean enable) {
		
		if (enable==true) { //connect 된 이후 enable 설정
			broker_id.setEnabled(false);
			broker_port.setEnabled(false);
			cb_broker_url.setEnabled(false);
			topic_pub.setEnabled(true);
			topic_sub.setEnabled(true);
			btn_topic_pub.setEnabled(true);
			btn_topic_sub.setEnabled(true);
			rdbtn_private.setEnabled(false);
			rdbtn_public.setEnabled(false);
						
			btnConnect.setText("DisConnect");
			btn_topic_pub.setText("set");
			btn_topic_sub.setText("set");
			return;
		}
		else { //disconnect 된 이후의 enable 설정
			topic_pub.setEnabled(false);
			topic_sub.setEnabled(false);
			btn_topic_pub.setEnabled(false);
			btn_topic_sub.setEnabled(false);
			rdbtn_private.setEnabled(true);
			rdbtn_public.setEnabled(true);
			if(rdbtn_private.isSelected()) {
				broker_id.setEnabled(true);
				broker_port.setEnabled(true);
				cb_broker_url.setEnabled(false);
			} else {
				broker_id.setEnabled(false);
				broker_port.setEnabled(false);
				cb_broker_url.setEnabled(true);
			}
			btnConnect.setText("Connect");
		}
	}
	
	//JScrollPane의 scroll 위치 조정
	public void scroll(JScrollPane sp, int vertical) {        
	    switch (vertical) {
	        case SwingConstants.TOP:
	        	sp.getVerticalScrollBar().setValue(0);
	            break;
	        case SwingConstants.CENTER:
	        	sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
	        	sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getValue() / 2);
	            break;
	        case SwingConstants.BOTTOM:  
	        	sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
	            break;
	    }
	}
	
	//#. 현재 MQTT 콜벡 메서드내에서 UI를 변경하기 위해서 내부 클래스로 정의 : 향후 핸들러를 이용하여 변경 필요
	class MyMQTT implements MqttCallback{
		private String broker;
		private String client_id;
		private String username;
		private String passwd;
		private MqttAsyncClient client;
		private MqttMessage message;
		private MemoryPersistence persistence;
		private MqttConnectOptions connOpts;
		//private String topic;
			
		public MyMQTT(String broker, String client_id,String username, String passwd){
			this.broker = broker;
			this.client_id = client_id;
			this.username = username;
			this.passwd = passwd;		
		}
		
		public void init(){			
			this.persistence = new MemoryPersistence();
			try {
				client = new MqttAsyncClient(this.broker, this.client_id, this.persistence);
				client.setCallback(this);
				connOpts = new MqttConnectOptions();
				if(client_id!=null && passwd != null){
					connOpts.setUserName(this.username);
					connOpts.setPassword(this.passwd.toCharArray());
				}
				connOpts.setCleanSession(true);
				System.out.println("Connecting to broker: "+this.broker);
				
				client.connect(connOpts);

				System.out.println("Connected");				
				message = new MqttMessage();
			} catch(MqttException me) {
				System.out.println("reason "+me.getReasonCode());
				System.out.println("msg "+me.getMessage());
				System.out.println("loc "+me.getLocalizedMessage());
				System.out.println("cause "+me.getCause());
				System.out.println("excep "+me);
				me.printStackTrace();
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void disconnect(){
			try {
				client.disconnect();
				client.close();
			} catch (MqttException e) {
				e.printStackTrace();
			}			 
		}
		
		public void publish(String topic, String msg, int qos){
			message.setQos(qos);
			message.setPayload(msg.getBytes());
			
			try {
				client.publish(topic, message);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		
		public void subscribe(String topic, int qos){
			try {
				client.subscribe(topic,qos);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
			String str = new String(mqttMessage.getPayload(), "UTF-8");
			
			if(str.equals("ConnectionTest")) { //최초 연결 체크를 위해서만 한번 확인
				System.out.println("연결이 확인되었습니다.");
				serverConnectedViewSetting(true);
				JOptionPane.showMessageDialog(MQTT_Client_ByKim.this, "연결에 성공하였습니다. \nTopic을 설정하여 주세요");	
			} else { //현재 설정 토픽과 동일한 경우 가져오기  				
				String time = (new SimpleDateFormat( "yy/MM/dd HH:mm:ss" )).format( Calendar.getInstance().getTime());				
				ta_sub.append(String.format("[%s] - subscribed\n  %s", time, str)+"\n");		
				scroll(scrollPane_sub,SwingConstants.BOTTOM);
			}
	    }

		@Override
		public void connectionLost(Throwable cause) {
			 System.out.println("Lost Connection." + cause.getCause());	
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
			System.out.println("Message with " + iMqttDeliveryToken + " delivered.");
		}
		
		public void msgBox(String msg) {
			JOptionPane.showMessageDialog(MQTT_Client_ByKim.this, msg);			
		}	

	}
}
