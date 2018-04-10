/*
 * Copyright (C) 2000 - 2018 Silverpeas
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.bridge;

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MobilFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

    Boolean mob = (Boolean) ((HttpServletRequest) req).getSession().getAttribute("isMobile");
    if (mob != null && !mob) {
      chain.doFilter(req, res);
      return;
    }

    String userAgent = ((HttpServletRequest) req).getHeader("User-Agent");
    if (userAgent != null) {
      boolean isMobile = userAgent.contains("Android") || userAgent.contains("iPhone");
      ((HttpServletRequest) req).getSession().setAttribute("isMobile", new Boolean(isMobile));

      Boolean tablet = (Boolean) ((HttpServletRequest) req).getSession().getAttribute("tablet");
      if (tablet == null) tablet = new Boolean(false);

      String url = ((HttpServletRequest) req).getRequestURL().toString();
      if (isMobile && !url.contains("services") && !url.contains("spmobile") && (!tablet)) {
        String params = "";
        if (url.contains("Publication")) {
          String id = url.substring(url.lastIndexOf("/") + 1);
          PublicationDetail pub = getPubBm().getDetail(new PublicationPK(id));
          String appId = pub.getInstanceId();
          params = "?shortcutContentType=Publication&shortcutContentId=" + id + "&shortcutAppId=" +
              appId;
        } else if (url.contains("Media")) {
          String id = url.substring(url.lastIndexOf("/") + 1);
          Media media = getGalleryService().getMedia(new MediaPK(id));
          String appId = media.getInstanceId();
          params = "?shortcutContentType=Media&shortcutContentId=" + id + "&shortcutAppId=" + appId;
        } else if(!url.contains("AuthenticationServlet") && (url.endsWith("silverpeas") || url.endsWith("silverpeas/") || url.contains("/silverpeas/"))) {
          // simple redirection on mobile login page
          params = "";
        } else {
          chain.doFilter(req, res);
          return;
        }
        ((HttpServletResponse) res).sendRedirect("/silverpeas/spmobile/spmobil.jsp" + params);
        return;
      } else {
        chain.doFilter(req, res);
        return;
      }
    } else {
      chain.doFilter(req, res);
      return;
    }
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

  private PublicationService getPubBm() {
    return PublicationService.get();
  }

  private GalleryService getGalleryService() {
    return MediaServiceProvider.getMediaService();
  }

}
