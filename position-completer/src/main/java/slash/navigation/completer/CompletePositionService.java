/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.completer;

import slash.navigation.common.LongitudeAndLatitude;
import slash.navigation.completer.elevation.ElevationLookupService;
import slash.navigation.download.DownloadManager;
import slash.navigation.earthtools.EarthToolsService;
import slash.navigation.geonames.GeoNamesService;
import slash.navigation.googlemaps.GoogleMapsService;
import slash.navigation.hgt.HgtFiles;
import slash.navigation.hgt.HgtFilesService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.lang.String.format;
import static slash.navigation.common.NavigationConversion.formatElevation;

/**
 * Helps to complement positions with elevation, postal address and populated place information.
 *
 * @author Christian Pesch
 */

public class CompletePositionService {
    private static final Logger log = Logger.getLogger(CompletePositionService.class.getName());
    protected static final Preferences preferences = Preferences.userNodeForPackage(CompletePositionService.class);
    private static final String ELEVATION_LOOKUP_SERVICE = "elevationLookupService";

    private final List<ElevationLookupService> elevationLookupServices = new ArrayList<ElevationLookupService>();
    private final HgtFilesService hgtFilesService;
    private final GeoNamesService geoNamesService = new GeoNamesService();
    private final GoogleMapsService googleMapsService = new GoogleMapsService();

    public CompletePositionService(DownloadManager downloadManager) {
        hgtFilesService = new HgtFilesService(downloadManager);
        for(HgtFiles hgtFile : hgtFilesService.getHgtFiles())
            elevationLookupServices.add(hgtFile);
        elevationLookupServices.add(geoNamesService);
        elevationLookupServices.add(googleMapsService);
        elevationLookupServices.add(new EarthToolsService());
    }

    public void dispose() {
        hgtFilesService.dispose();
    }

    public List<ElevationLookupService> getElevationLookupServices() {
        return elevationLookupServices;
    }

    public ElevationLookupService getElevationLookupService() {
        String lookupServiceName = preferences.get(ELEVATION_LOOKUP_SERVICE, elevationLookupServices.get(0).getName());

        for (ElevationLookupService service : elevationLookupServices) {
            if (lookupServiceName.endsWith(service.getName()))
                return service;
        }

        log.warning(format("Failed to find elevation lookup service %s; using GeoNames", lookupServiceName));
        return geoNamesService;
    }

    public void setElevationLookupService(ElevationLookupService service) {
        preferences.put(ELEVATION_LOOKUP_SERVICE, service.getName());
    }

    public Double getElevationFor(double longitude, double latitude) throws IOException {
        Double elevation = getElevationLookupService().getElevationFor(longitude, latitude);
        return elevation != null ? formatElevation(elevation).doubleValue() : null;
    }

    public String getDescriptionFor(double longitude, double latitude) throws IOException {
        String description = googleMapsService.getLocationFor(longitude, latitude);
        if (description == null)
            description = geoNamesService.getNearByFor(longitude, latitude);
        return description;
    }

    public void downloadElevationFor(List<LongitudeAndLatitude> longitudeAndLatitudes) {
        ElevationLookupService service = getElevationLookupService();
        if(service instanceof HgtFiles) {
            HgtFiles hgtFiles = (HgtFiles) service;
            hgtFiles.downloadElevationFor(longitudeAndLatitudes);
        }
    }
}
