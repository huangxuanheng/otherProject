package com.demo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.incrementalupdates.R;
import com.nothome.delta.Delta;
import com.nothome.delta.DiffWriter;
import com.nothome.delta.GDiffWriter;

/**
 * 增量更新
 * 
 * @author 小孩子a
 * 
 *         注：实际工作中，各个路径，需要根据实际情况传入。demo中偷懒下~
 * 
 * */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button btnCreate = (Button) findViewById(R.id.btnCreate);
		Button btnMix = (Button) findViewById(R.id.btnMix);
		Button btnInstall = (Button) findViewById(R.id.btnInstall);

		btnCreate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(){
					public void run() {
						createPatch();
					};
				}.start();
				
			}
		});
		btnMix.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(){
					public void run() {
						mixPatch();
					};
				}.start();
				
				
			}
		});
		btnInstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				installAPK();
			}
		});
		
		
		List<ApplicationInfo> installedApplications = getPackageManager().getInstalledApplications(0);
		for(ApplicationInfo info:installedApplications){
			System.out.println(info.sourceDir);
			
		}
		
		try {
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.ishow.app", 0);
			System.out.println(applicationInfo.sourceDir+"===========");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 生成差分包：old_new.patch = diff(old.apk, old.apk)
	 * 
	 * */
	private void createPatch() {
		try {
			String sd = Environment.getExternalStorageDirectory().getPath()+"/huiyuan";
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.ishow.app", 0);
			String oldFile = applicationInfo.sourceDir;
			String newFile = sd + "/new.apk";
			String patchFile = sd + "/old_new.patch";

			DiffWriter output = null;
			File sourceFile = null;
			File targetFile = null;

			sourceFile = new File(oldFile);
			targetFile = new File(newFile);
			output = new GDiffWriter(new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							patchFile)))));

			if (sourceFile.length() > Integer.MAX_VALUE
					|| targetFile.length() > Integer.MAX_VALUE) {
				System.err
						.println("source or target is too large, max length is "
								+ Integer.MAX_VALUE);
				System.err.println("aborting..");

			}
			
			Delta d = new Delta();
			d.compute(sourceFile, targetFile, output);
			Looper.prepare();
			Toast.makeText(getApplicationContext(), "生成完成！", Toast.LENGTH_LONG)
					.show();
			Looper.loop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 合成差分包：new.apk = old.apk + old_new.patch
	 * */
	private void mixPatch() {
		try {

			String sd = Environment.getExternalStorageDirectory()
					.getAbsolutePath()+"/huiyuan";
			String serviceFile = sd + "/new.apk";
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.ishow.app", 0);
			String source = applicationInfo.sourceDir;
			String patch = sd + "/old_new.patch";

			String target = sd + "/mix.apk";

			String newMD5 = DiffTool.getMD5(new File(serviceFile));

			DiffTool.mergeApk(source, patch, target, newMD5);
			Looper.prepare();
			Toast.makeText(getApplicationContext(), "合成完成！", Toast.LENGTH_LONG)
					.show();
			Looper.loop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 安装apk。 这边路径已经写死，实际应用中，apk路径需要当参数传入
	 * */
	private void installAPK() {
		File apkfile = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/huiyuan/mix.apk");
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		MainActivity.this.startActivity(i);

	}

}
