package bi.meteorite.plugin;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityParameterProvider;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by bugg on 13/01/15.
 */
@Component
@Path("cm4/api/endpoints")
public class Endpoints {

  App a = new App();

  protected boolean isAdmin() {
    SecurityParameterProvider securityParams = new SecurityParameterProvider( PentahoSessionHolder.getSession() );
    return securityParams.getParameter( "principalAdministrator" ).equals( "true" );
  }

  @GET
  @Produces({"application/json"})
  @Path("/exists")
  public Response exists(){


    CM4Version v = new CM4Version(a.getInstalledVersion());
    return Response.ok().entity(v).build();
  }

  @POST
  public Response installLibs(@QueryParam("version") String version){

    if(isAdmin()) {
      a.loadFiles(version);

      return Response.ok().build();
    }
    else{
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  @GET
  @Produces({"application/json" })
  public Response checkForUpdates(){

    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  @Produces({"application/json" })
  @Path("/version")
  public Response checkEEvsCE(){
    BIVersion v = a.getVersion();
    return Response.ok().entity(v).build();
  }
}
