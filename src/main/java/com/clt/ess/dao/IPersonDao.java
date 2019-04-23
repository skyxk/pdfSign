package com.clt.ess.dao;


import com.clt.ess.entity.Person;

import java.util.List;

public interface IPersonDao {
    /**
     * 查询个人信息
     * @return person对象
     */
    Person findPerson(Person person);

    /**
     * 根据id 查找person
     * @param personId id
     * @return person对象
     */
    Person findPersonById(String personId);
    /**
     * 根据关键字查找人员列表
     * @param keyword 关键词
     */
    List<Person> findPersonListByKeyword(String keyword);

    /**
     * 根据全省统一人员id 查找person
     * @param provincialUserId 全省统一人员id
     * @return person对象
     */
    Person findPersonByProvincialUserId(String provincialUserId);
    /**
     * 根据身份证号 查找person
     * @param idNum 身份证号
     * @return person对象
     */
    Person findPersonByIdNum(String idNum);
}
