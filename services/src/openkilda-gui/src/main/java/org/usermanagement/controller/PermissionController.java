package org.usermanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openkilda.constants.IConstants;
import org.usermanagement.model.Permission;
import org.usermanagement.model.UserInfo;
import org.usermanagement.service.PermissionService;

@RestController
@RequestMapping(path = "/permission", produces = MediaType.APPLICATION_JSON_VALUE)
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST)
    public Permission create(@RequestBody final Permission request) {
        Permission permissionResponse = permissionService.createPermission(request);

        return permissionResponse;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public List<Permission> getPermissionList(final HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute(IConstants.SESSION_OBJECT);
        return permissionService.getAllPermission(userInfo.getUserId());
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{permission_id}", method = RequestMethod.GET)
    public Permission getPermissionById(@PathVariable("permission_id") final Long permissionId) {
        return permissionService.getPermissionById(permissionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{permission_id}", method = RequestMethod.DELETE)
    public void deletePermissionById(@PathVariable("permission_id") final Long permissionId) {
        permissionService.deletePermissionById(permissionId);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{permission_id}", method = RequestMethod.PUT)
    public Permission updatePermission(@PathVariable("permission_id") final Long permissionId,
            @RequestBody final Permission request) {
        Permission permissionResponse = permissionService.updatePermission(permissionId, request);
        return permissionResponse;
    }
}
