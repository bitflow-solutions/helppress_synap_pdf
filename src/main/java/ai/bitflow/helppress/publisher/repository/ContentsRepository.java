package ai.bitflow.helppress.publisher.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.idclass.PkContents;


@Repository
public interface ContentsRepository extends JpaRepository <Contents, PkContents> {

	List<Contents> findAllByGroupId(String groupId);
	
}
