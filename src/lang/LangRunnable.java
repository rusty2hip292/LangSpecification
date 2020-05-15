package lang;

import java.util.ArrayList;

public interface LangRunnable {

	void run();
	void check() throws Exception;
}

class LangSequencer implements LangRunnable {
	
	ArrayList<? extends LangRunnable> seq = new ArrayList<LangRunnable>();
	
	public void run() {
		for(LangRunnable lr : seq) {
			lr.run();
		}
	}
	public void check() throws Exception {
		for(LangRunnable lr : seq) {
			lr.check();
		}
	}
}

class LangIf implements LangRunnable {
	
	private final LangValue lv;
	private final LangRunnable tcase, fcase;
	public LangIf(LangValue lv, LangRunnable tcase, LangRunnable fcase) {
		this.lv = lv; this.tcase = tcase; this.fcase = fcase;
	}
	
	public void run() {
		//if(lv.asCondition()) {
		//	tcase.run();
		//}else {
		//	fcase.run();
		//}
	}
	
	public void check() throws Exception {
		//lv.checkCanBeCondition();
		tcase.check();
		fcase.check();
	}
}
