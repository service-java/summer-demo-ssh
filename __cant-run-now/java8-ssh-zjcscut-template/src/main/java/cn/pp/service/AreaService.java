package cn.pp.service;

import cn.pp.common.dao.AreaDAO;
import cn.pp.dto.AreaDTO;
import cn.pp.entity.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zjc
 * @version 2016/5/16 22:10
 */
@Service
public class AreaService {

    @Autowired
    private AreaDAO areaDAO;



    public Area findById(Integer id) {
        return areaDAO.get(id);
    }

    public Area findArea() {
        return areaDAO.findArea();
    }

    public List<Area> getByCondition() {
        Map<String, Object> params = new HashMap<>();
        Object[] ids = {1, 2, 3};
        params.put("id", ids);
        return areaDAO.getObjectList(params);
    }

    public AreaDTO getBySQLAndTransBean() {
        return areaDAO.getByNativeSQLAndTransferBean(1);
    }

    public Area getByNativeSQL() {
        return areaDAO.getByNativeSQL(1);
    }

    public Area getByHQL() {
        return areaDAO.selectByHql(2);
    }

    //删除area，级联删除district
    public void deleteArea() {
        areaDAO.del(2);
    }


    //HQL联查
    public AreaDTO selectByHQL() {
        return areaDAO.selectByHqlTransDTO(1);
    }


    public void updateArea(Integer id) {
        Area a = areaDAO.selectByHql(id);
        a.setName("update测试");
        areaDAO.update(a);
    }

    public Area getFromSecondCache(Integer id) {
        return areaDAO.selectFromSecondCache(id);
    }

    public Map getSecondLevelCacheInfo() {
        return areaDAO.monitorSecondLevelCache();  //查看二级缓存
    }

    public List<Area> selectAllArea() {
        return areaDAO.getObjectList(null);
    }

    public List<Area> selectBySqlContext(Map<String,Object> params,Map<String,String> filter){
//        return  sqlDao.selectAreaByCondition(params, filter);
		return null;
    }

}
