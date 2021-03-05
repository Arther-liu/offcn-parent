package com.offcn.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.conatant.ProjectConstant;
import com.offcn.project.enums.ProjectImageTypeEnume;
import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectCreateServiceImpl implements ProjectCreateService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TProjectMapper projectMapper;
    @Resource
    private TProjectImagesMapper projectImagesMapper;
    @Resource
    private TProjectTagMapper projectTagMapper;
    @Resource
    private TProjectTypeMapper projectTypeMapper;
    @Resource
    private TReturnMapper returnMapper;
    @Override
    public String initCreateProject(Integer memberId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        ProjectRedisStorageVo vo = new ProjectRedisStorageVo();
        //存令牌，存id
        vo.setMemberid(memberId);
        vo.setProjectToken(token);
        String jsonString = JSON.toJSONString(vo);
        stringRedisTemplate.opsForValue().set(ProjectConstant.TEMP_PROJECT_PREFIX+token,jsonString);
        return token;
    }

    @Override
    public void saveProjectInfo(ProjectStatusEnume auth, ProjectRedisStorageVo projectVo) {
        //1.创建新项目
        TProject project = new TProject();
        BeanUtils.copyProperties(projectVo,project);//将原项目的属性值赋值给新建的项目
        //设置事件
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        project.setCreatedate(format);
        //设置项目的状态
        project.setStatus(auth.getCode()+"");
        //保存到数据库
        projectMapper.insertSelective(project);
        //2.拿到项目的id
        Integer projectId = project.getId();
        //3.保存图片
        String headerImage = projectVo.getHeaderImage();
        TProjectImages tprojectImages = new TProjectImages(null, projectId, headerImage, ProjectImageTypeEnume.HEADER.getCode());
        projectImagesMapper.insertSelective(tprojectImages);
        //详图
        List<String> detailsImage = projectVo.getDetailsImage();
        if(!CollectionUtils.isEmpty(detailsImage)){
            for (String s : detailsImage) {
                TProjectImages detail = new TProjectImages(null, projectId, s, ProjectImageTypeEnume.DETAILS.getCode());
                projectImagesMapper.insertSelective(detail);

            }
        }
        //4.标签
        List<Integer> tagids = projectVo.getTagids();
        if(!CollectionUtils.isEmpty(tagids)){
            for (Integer tagid : tagids) {
                TProjectTag tProjectTag = new TProjectTag(null, projectId, tagid);
                projectTagMapper.insertSelective(tProjectTag);
            }
        }
        //5.保存分类
        List<Integer> typeids = projectVo.getTypeids();
        if(!CollectionUtils.isEmpty(typeids)){
            for (Integer typeid : typeids) {
                TProjectType tProjectType = new TProjectType(null, projectId, typeid);
                projectTypeMapper.insertSelective(tProjectType);
            }
        }
        //6.保存汇报
        List<TReturn> projectReturns = projectVo.getProjectReturns();
        if(!CollectionUtils.isEmpty(projectReturns)){
            for (TReturn tReturn : projectReturns) {
                tReturn.setProjectid(projectId);
                returnMapper.insertSelective(tReturn);
            }
        }
        //7.清空redis
        stringRedisTemplate.delete(ProjectConstant.TEMP_PROJECT_PREFIX+projectVo.getProjectToken());
    }
}
