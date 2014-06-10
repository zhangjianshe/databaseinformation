package cn.polatu.tools.database.gen;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Context {

	public Context() {
		SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		unitComment = new Comment(0);
		unitComment.addLine(df.format(new Date())+"  database api exporter V4.0");
		unitComment
				.addLine("============================================================");
		unitComment.addLine("#  ____ _  _ ____ _  _ ____  _ _ ____ _  _ ____ _  _ ____  #");
		unitComment.addLine("#   __] |__| |__| |\\ | | __  | | |__| |\\ | [__  |__| |___  #");
		unitComment.addLine("#  [___ |  | |  | | \\| |__| _| | |  | | \\| ___] |  | |___  #");
		unitComment.addLine("#           http://hi.baidu.com/zhangjianshe               #");
		unitComment.addLine("============================================================");
		
		
		
	}

	Comment unitComment;
	private String mBasePath;
	private String packageName;
	private String schema;
	private String output="";
	
	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getBasePath() {
		return mBasePath;
	}

	public void setBasePath(String mBasePath) {
		this.mBasePath = mBasePath;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Comment getUnitComment() {
		return unitComment;
	}

	public void setUnitComment(Comment unitComment) {
		this.unitComment = unitComment;
	}

}
