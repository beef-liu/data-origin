package ${basePackage}.context;

import javax.servlet.ServletContext;

import com.beef.dataorigin.web.context.DataOriginWebContext;
import com.salama.service.clouddata.core.AppContext;
import com.salama.service.clouddata.core.AppServiceContext;

public class MyDataOriginWebContext extends DataOriginWebContext {

	@Override
	public void reload(ServletContext servletContext, String configLocation) {
		AppContext appContext = AppServiceContext.getAppContext();
		super.reload(servletContext, configLocation, appContext);
	}
	
}
