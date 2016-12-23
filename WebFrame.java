import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


public class WebFrame extends JFrame 
{
	private DefaultTableModel model;
	private JTable table;
	private JTextField threadNum;
	private JLabel running;
	private JLabel complete;
	private JLabel elapsed;
	private JProgressBar progress;
	private Thread launcher;
	private int runCount;
	private int completeCount;

	private JButton single;
	private JButton concurrent;
	private JButton stop;

	private long startTime;

	private int hold;
	private boolean input;

	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				new WebFrame(args[0]); 
			}
		});
	}

	public WebFrame (String filename)
	{
		runCount=0;
		input=false;
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


		try
		{
			Scanner scan =new Scanner(new File(filename));
			while(scan.hasNext())
			{
				String next = scan.nextLine();
				model.addRow(new String[] { next, ""});
				
			}
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		}


		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel, BorderLayout.SOUTH);
		

		single = new JButton("Single Thread Fetch");
		concurrent = new JButton("Concurrent Fetch");
		stop = new JButton("Stop");
		stop.setEnabled(false);



		threadNum = new JTextField("Enter the number of threads wanted to use");


		running = new JLabel("Running:0");
		complete = new JLabel("Completed:0");
		elapsed = new JLabel("Elapsed:");
		progress = new JProgressBar(0, model.getRowCount());


		single.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				startTime = System.currentTimeMillis();
				single.setEnabled(false);
				concurrent.setEnabled(false);
				stop.setEnabled(true);
				for (int i = 0; i < model.getRowCount(); i++)  //reset model
				{ 
					model.setValueAt("", i, 1);
					
				}
				progress.setValue(0);
				running.setText("Running:0");
				elapsed.setText("Elapsed:");
				complete.setText("Completed:0");
				runCount =0;
				completeCount = 0;
				launcher = new Launcher(1);
				launcher.start();
				
			}
		});

		concurrent.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				startTime = System.currentTimeMillis();
				single.setEnabled(false);
				concurrent.setEnabled(false);
				stop.setEnabled(true);
				
				try
				{
					int numThreads = Integer.parseInt(threadNum.getText());
					if(numThreads <=0)
					{
						System.out.println("Enter a postive integer" );
						single.setEnabled(true);
						concurrent.setEnabled(true);
						stop.setEnabled(false);
						input=false;
					}
					else
					{	
						input=true;
						
					}
					launcher = new Launcher(numThreads);
					launcher.start();
					
				}
				catch(NumberFormatException f)
				{
					single.setEnabled(true);
					concurrent.setEnabled(true);
					stop.setEnabled(false);
					input=false;
					System.out.println("Enter an integer");
				}
				
				for (int i = 0; i < model.getRowCount(); i++)  //reset model
				{
					model.setValueAt("", i, 1);
					
				}
				progress.setValue(0);
				runCount=0;
				running.setText("Running:0");
				elapsed.setText("Elapsed:");
				complete.setText("Completed:0");
				completeCount = 0;
				
				
				
				





			}
		}
				);

		stop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				single.setEnabled(true);
				concurrent.setEnabled(true);
				stop.setEnabled(false);
				launcher.interrupt();

			}
		}
				);

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 300));
		add(scrollpane);

		JFrame frame =new JFrame();
		frame.setTitle("WebLoader");
		frame.add(scrollpane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
		buttonPanel.add(single);
		buttonPanel.add(concurrent);
		buttonPanel.add(threadNum);
		buttonPanel.add(stop);



		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.Y_AXIS));
		labelPanel.add(running);
		labelPanel.add(complete);
		labelPanel.add(elapsed);
		labelPanel.add(progress,BorderLayout.SOUTH);


		frame.add(buttonPanel,BorderLayout.EAST);
		frame.add(labelPanel,BorderLayout.WEST);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	//mainly got help from tutors here
	private class Launcher extends Thread 
	{
		private Semaphore semaphore;
		public Launcher(int numThread) 
		{
			semaphore = new Semaphore(numThread);

		}

		@Override
		public void run()
		{
			ArrayList<Thread> threadsArray = new ArrayList<Thread>();
			runIncrease();
			for (int i = 0; i < model.getRowCount(); i++) {

				WebWorker worker = new WebWorker( WebFrame.this,(String)model.getValueAt(i, 0), i, semaphore);
				worker.start();
				threadsArray.add(worker);
			}

			for (Thread t:threadsArray)
			{
				try
				{
					t.join();
				} 
				catch (InterruptedException e) 
				{
					for (Thread w:threadsArray) 
					{
						w.interrupt();
					}
					break;
				}
			}

			runDecrease();
			single.setEnabled(true);
			concurrent.setEnabled(true);
			stop.setEnabled(false);
		}
	}



	public synchronized void runIncrease()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() 
			{
				runCount++;
				running.setText("Running:"+ Integer.toString(runCount));//here
			}
		});
		

	}

	public synchronized void runDecrease() 
	{
		
		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				runCount--;
				running.setText("Running:"+ Integer.toString(runCount));

			}
		});
		


	}

	public synchronized void completeRun(final int rowNum, final String status)
	{
		//webFrame.completeRun(rowNum,"Running");
		completeCount++;
		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() {
				complete.setText("Completed:" + Integer.toString(completeCount));
				progress.setValue(completeCount);
				model.setValueAt(status, rowNum, 1);
			}
		});

	}
	

	public synchronized void setRunningStatus(final int rowNum, final String status)
	{
	
		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run()
			{
				model.setValueAt(status, rowNum, 1);
			}
		});

	}
	
	



	public synchronized void changeTime() 
	{
		final long elapsedTime = System.currentTimeMillis() - startTime;

		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				elapsed.setText("Elapsed:" + elapsedTime);

			}
		});
	}
}