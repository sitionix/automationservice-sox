package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentProjectsImplTest {

    private GetAgentProjectsImpl getAgentProjects;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentProjects = new GetAgentProjectsImpl(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenNullQuery_whenExecute_thenUseDefaultPagination() {
        //given
        final AgentProjectsPage expected = mock(AgentProjectsPage.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findAllVisibleByOwnerUserId(17L, 0, 20)).thenReturn(expected);

        //when
        final AgentProjectsPage actual = this.getAgentProjects.execute(null);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findAllVisibleByOwnerUserId(17L, 0, 20);
    }

    @Test
    void givenInvalidPage_whenExecute_thenThrowValidationException() {
        //given
        final GetAgentProjectsQuery query = this.getQuery(-1, 20);

        //when
        //then
        assertThatThrownBy(() -> this.getAgentProjects.execute(query))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Page must be greater than or equal to 0");

        verifyNoInteractions(this.authenticatedUserProvider, this.agentProjectRepository);
    }

    @Test
    void givenInvalidSize_whenExecute_thenThrowValidationException() {
        //given
        final GetAgentProjectsQuery query = this.getQuery(0, 101);

        //when
        //then
        assertThatThrownBy(() -> this.getAgentProjects.execute(query))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Size must be between 1 and 100");

        verifyNoInteractions(this.authenticatedUserProvider, this.agentProjectRepository);
    }

    private GetAgentProjectsQuery getQuery(final Integer page, final Integer size) {
        return GetAgentProjectsQuery.builder()
                .page(page)
                .size(size)
                .build();
    }
}
